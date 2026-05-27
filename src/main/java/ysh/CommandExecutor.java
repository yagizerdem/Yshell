package ysh;


import ysharp.treewalk.YsharpException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {

    public Type.CommandExecutionResponse ExecuteCommand(Type.Command command) {
        Type.CommandExecutionOptions options = Type.CommandExecutionOptions.defaults();
        return ExecuteCommand(command, options);
    }

    public Type.CommandExecutionResponse ExecuteCommand(Type.Command command,
                                                      Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        response.stdOut = "";
        response.stdErr = "";
        if(command.isBuiltIn) {
            return ExecuteBuiltIn(command, options);
        }
        StringBuilder stdOut = new StringBuilder();
        StringBuilder stdErr = new StringBuilder();

        try {
            ProcessBuilder pb =
                    new ProcessBuilder(command.args);
            pb.redirectErrorStream(true); // transfer stderr to stdout

            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if(options.captureStdout) {
                        stdOut.append(line);
                    }
                    else {
                        System.out.println(line);
                    }
                }
            }

            p.waitFor();
            Context.getContext().setExitStatus(p.exitValue());
        }catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.out.println(ex.getMessage());
        }

        if(options.captureStdout) {
            response.stdOut = stdOut.toString();
        }

        return response;
    }

    public Type.CommandExecutionResponse ExecutePipe(Type.Pipe pipe) {
        Type.CommandExecutionOptions options = Type.CommandExecutionOptions.defaults();
        return ExecutePipe(pipe, options);
    }

    public Type.CommandExecutionResponse ExecutePipe(Type.Pipe pipe, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();

        try{
            List<Thread> threads = new ArrayList<>();
            List<Process> processes = new ArrayList<>();

            for (int i = 0; i < pipe.commands.size(); i++) {
                Type.Command shellCommand = (Type.Command) pipe.commands.get(i);

                ProcessBuilder pb =
                        new ProcessBuilder(shellCommand.args);
                pb.redirectErrorStream(true);

                processes.add(pb.
                        redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start());
            }


            for (int i = 0; i < processes.size() - 1; i++) {
                Process left = processes.get(i);
                Process right = processes.get(i + 1);

                threads.add(new Thread(() -> {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(left.getInputStream()));
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(right.getOutputStream()));
                    String lineToPipe;

                    try {

                        while ((lineToPipe = bufferedReader.readLine()) != null) {
                            bufferedWriter.write(lineToPipe);
                            bufferedWriter.newLine();
                            bufferedWriter.flush();
                        }
                        bufferedWriter.close();

                    } catch (IOException e) {

                    }
                }));
            }

            threads.add(new Thread(() -> {
                Process last = processes.getLast();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(last.getInputStream()));
                try {
                    String lineToPipe;
                    while ((lineToPipe = bufferedReader.readLine()) != null) {
                        System.out.println(lineToPipe);
                    }

                    last.waitFor();
                    Context.getContext().setExitStatus(last.exitValue());
                } catch (Exception e) {
                    Context.getContext().setExitStatus(1);
                }
            }));

            for (Thread th : threads) {
                th.start();
            }

            for (Thread th : threads) {
                th.join();
            }
        }catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.out.println(ex.getMessage());
        }

        return response;
    }

    public Type.CommandExecutionResponse ExecuteConditionalCommand(Type.ConditionalCommand chainCommand) {
        Type.CommandExecutionOptions options = Type.CommandExecutionOptions.defaults();
        return ExecuteConditionalCommand(chainCommand, options);
    }

    public Type.CommandExecutionResponse ExecuteConditionalCommand(Type.ConditionalCommand chainCommand, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        chainCommand.command.execute(this);

        int exitStatus = Context.getContext().exitStatus;
        if(chainCommand.chainCommand == null) {
            return response;
        }
        if(exitStatus == 0 && chainCommand.operator.type == Type.TokenType.AND_CONDITIONAL) {
            chainCommand.chainCommand.execute(this);
        }
        if(exitStatus != 0 && chainCommand.operator.type == Type.TokenType.OR_CONDITIONAL) {
            chainCommand.chainCommand.execute(this);
        }

        return response;
    }

    public Type.CommandExecutionResponse ExecuteGroupedCommand(Type.GroupedCommand groupedCommand) {
        Type.CommandExecutionOptions options = Type.CommandExecutionOptions.defaults();
        return ExecuteGroupedCommand(groupedCommand, options);
    }
    public Type.CommandExecutionResponse ExecuteGroupedCommand(Type.GroupedCommand groupedCommand, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        for(Type.BaseCommand command : groupedCommand.commands) {
            command.execute(this);
        }
        return response;
    }

    public Type.CommandExecutionResponse ExecuteBuiltIn(Type.Command command) {
        return ExecuteBuiltIn(command, Type.CommandExecutionOptions.defaults());
    }

    public Type.CommandExecutionResponse ExecuteBuiltIn(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        // command dispatcher util
        switch (command.args.getFirst()) {
            case "cd" :
            case "chdir" : {
                return ExecuteCd(command, options);
            }
            case "echo" : {
                return ExecuteEcho(command, options);
            }
            case "exit" : {
                return ExecuteExit(command, options);
            }
            case "dir":
            case "ls": {
                return ExecuteDir(command, options);
            }
            case "set": {
                return ExecuteSet(command, options);
            }
            case "mkdir" : {
                return ExecuteMkdir(command, options);
            }
        }
        return response;
    }

    public Type.CommandExecutionResponse ExecuteCd(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        try {
            if(command.args.size() != 2) {
                System.out.println("cd args should max size 2");
                return response;
            }

            String dest = command.args.get(1);
            Context context = Context.getContext();

            if(dest.equals(".")) {
                return response;
            }

            if(dest.equals("..")) {
                Path parent = context.cwd.getParent();

                if (parent == null) {
                    return response;
                }

                if (!Files.exists(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "cd: no such file or directory: " + dest
                    );
                }

                if (!Files.isDirectory(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "cd: not a directory: " + dest
                    );
                }

                context.cwd = parent;
                System.setProperty("user.dir", context.cwd.toString());
                return response;
            }

            Path currentPath = context.cwd;

            Path newPath = Paths.get(dest);
            if (!newPath.isAbsolute()) {
                newPath = currentPath.resolve(dest);
            }

            newPath = newPath.normalize();

            if (!Files.exists(newPath)) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "cd: no such file or directory: " + dest
                );
            }

            if (!Files.isDirectory(newPath)) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "cd: not a directory: " + dest
                );
            }

            context.cwd = newPath;
            System.setProperty("user.dir", newPath.toString());

            Context.getContext().setExitStatus(0);
        }catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return response;
    }

    public Type.CommandExecutionResponse ExecuteEcho(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();

        try {
            if (command.args.size() == 1) {
                System.out.println();
                Context.getContext().setExitStatus(0);
                return response;
            }

            StringBuilder builder = new StringBuilder();

            for (int i = 1; i < command.args.size(); i++) {
                if (i > 1) {
                    builder.append(" ");
                }

                builder.append(command.args.get(i));
            }

            if(options.captureStdout) {
                response.stdOut += builder.toString();
            }
            else {
                System.out.println(builder.toString());
            }
            Context.getContext().setExitStatus(0);

        } catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.err.println(ex.getMessage());
        }
        return response;
    }

    public Type.CommandExecutionResponse  ExecuteExit(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        try {
            if(command.args.size() > 2) {
                System.out.println("exit args should max size 2");
                return response;
            }

            int exitStatus = 0;
            if(command.args.size() == 2) {
               String status = command.args.get(1);
               try {
                   exitStatus = Integer.parseInt(status);
               } catch (NumberFormatException ex) {
                   Context.getContext().setExitStatus(1);
                   System.out.println("exit: numeric argument required: " + status);
                   return response;
               }
            }
            System.exit(exitStatus);
        }
        catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.err.println(ex.getMessage());
        }
        return response;
    }

    public Type.CommandExecutionResponse  ExecuteDir(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        try {
            Context context = Context.getContext();

            if(command.args.size() == 1) {
                File file = new File(context.cwd.toString());
                String[] entities = file.list();
                for(String entity : entities) {
                    System.out.println(entity);
                }
                return response;
            }

            for(int i = 1; i < command.args.size(); i++) {
                String part = command.args.get(i);
                Path newPath = Paths.get(part);

                if(!newPath.isAbsolute()) {
                    newPath = context.cwd.resolve(newPath);
                }

                newPath = newPath.normalize();

                if(Files.isDirectory(newPath)) {
                    File file = new File(newPath.toString());
                    String[] entities = file.list();
                    for(String entity : entities) {
                        System.out.println(entity);
                    }
                }
                else {
                    System.out.println(newPath);
                }
            }
        }
        catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return response;
    }

    public Type.CommandExecutionResponse  ExecuteSet(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        Context context = Context.getContext();
        try {
            if (command.args.size() != 2) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: expected exactly one assignment in the form name=value"
                );
            }

            String assignment = command.args.get(1);
            Cursor cur = new Cursor(Preprocess.preprocess(assignment));

            StringBuilder identifierPart = new StringBuilder();
            StringBuilder valuePart = new StringBuilder();

            if (cur.isEnd()) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: assignment cannot be empty"
                );
            }

            if (cur.peek().c == '=') {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: missing variable name before '='"
                );
            }

            Type.Pchar first = cur.peek();

            if (!cur.isAlphaOrUnderscore(first)) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: invalid variable name: variable name must start with a letter or '_'"
                );
            }

            while (!cur.isEnd()) {
                Type.Pchar pChar = cur.peek();

                if (pChar.c == '=') {
                    cur.advance();
                    break;
                }

                if (!cur.isAlphaNumericOrUnderscore(pChar)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "set: invalid variable name: character '" + pChar.c + "' is not allowed"
                    );
                }

                identifierPart.append(cur.advance());
            }

            if (identifierPart.length() == 0) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: variable name cannot be empty"
                );
            }

            if (cur.prev().c != '=') {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: missing '=' in assignment"
                );
            }

            while (!cur.isEnd()) {
                valuePart.append(cur.advance());
            }

            context.env.setVariable(
                    identifierPart.toString(),
                    valuePart.toString()
            );

            context.setExitStatus(0);

        } catch (Exception ex) {
            context.setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return response;
    }

    public Type.CommandExecutionResponse  ExecuteMkdir(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        Context context = Context.getContext();

        try {
            if (command.args.size() < 2) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "mkdir: missing operand"
                );
            }

            for (int i = 1; i < command.args.size(); i++) {
                String part = command.args.get(i);

                if (part == null || part.isBlank()) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: invalid empty directory name"
                    );
                }

                Path newPath = Paths.get(part);

                if (!newPath.isAbsolute()) {
                    newPath = context.cwd.resolve(newPath);
                }

                newPath = newPath.normalize();

                if (Files.exists(newPath)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: cannot create directory '" + part + "': file exists"
                    );
                }

                Path parent = newPath.getParent();

                if (parent != null && !Files.exists(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: cannot create directory '" + part + "': parent directory does not exist"
                    );
                }

                if (parent != null && !Files.isDirectory(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: cannot create directory '" + part + "': parent is not a directory"
                    );
                }

                Files.createDirectory(newPath);
            }

            context.setExitStatus(0);

        } catch (Exception ex) {
            context.setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return  response;
    }
}
