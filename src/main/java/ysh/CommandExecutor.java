package ysh;


import ysharp.treewalk.YsharpException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {

    public void ExecuteCommand(Type.Command command) {
        if(command.isBuiltIn) {
            ExecuteBuiltIn(command);
            return;
        }

        try {
            ProcessBuilder pb =
                    new ProcessBuilder(command.args);
            pb.redirectErrorStream(true); // transfer stderr to stdout

            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            p.waitFor();
            Context.getContext().setExitStatus(p.exitValue());
        }catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.out.println(ex.getMessage());
        }
    }

    public void ExecutePipe(Type.Pipe pipe) {

        try{
            List<Thread> threads = new ArrayList<>();
            List<Process> processes = new ArrayList<>();

            for (int i = 0; i < pipe.commands.size(); i++) {
                Type.Command shellCommand = pipe.commands.get(i);

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
    }

    public void ExecuteChainCommand(Type.ChainCommand chainCommand) throws YsharpException {
        ExecuteCommand(chainCommand.command);
        int exitStatus = Context.getContext().exitStatus;
        if(chainCommand.chainCommand == null) {
            return;
        }
        if(exitStatus == 0 && chainCommand.operator.type == Type.TokenType.AND_CONDITIONAL) {
            ExecuteChainCommand(chainCommand.chainCommand);
        }
        if(exitStatus != 0 && chainCommand.operator.type == Type.TokenType.OR_CONDITIONAL) {
            ExecuteChainCommand(chainCommand.chainCommand);
        }
    }

    public void ExecuteBuiltIn(Type.Command command) {
        // command dispatcher util
        switch (command.args.getFirst()) {
            case "cd" : {
                ExecuteCd(command);
                break;
            }
            case "echo" : {
                ExecuteEcho(command);
                break;
            }
            case "exit" : {
                ExecuteExit(command);
                break;
            }
        }
    }

    public void ExecuteCd(Type.Command command) {

        try {
            if(command.args.size() != 2) {
                System.out.println("cd args should max size 2");
                return;
            }

            String dest = command.args.get(1);
            Context context = Context.getContext();

            if(dest.equals(".")) {
                return;
            }

            if(dest.equals("..")) {
                Path parent = context.cwd.getParent();

                if (parent == null) {
                    return;
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
                return;
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
    }

    public void ExecuteEcho(Type.Command command) {
        try {
            if (command.args.size() == 1) {
                System.out.println();
                Context.getContext().setExitStatus(0);
                return;
            }

            StringBuilder builder = new StringBuilder();

            for (int i = 1; i < command.args.size(); i++) {
                if (i > 1) {
                    builder.append(" ");
                }

                builder.append(command.args.get(i));
            }

            System.out.println(builder.toString());
            Context.getContext().setExitStatus(0);

        } catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.err.println(ex.getMessage());
        }
    }

    public void ExecuteExit(Type.Command command) {
        try {
            if(command.args.size() > 2) {
                System.out.println("exit args should max size 2");
                return;
            }

            int exitStatus = 0;
            if(command.args.size() == 2) {
               String status = command.args.get(1);
               try {
                   exitStatus = Integer.parseInt(status);
               } catch (NumberFormatException ex) {
                   Context.getContext().setExitStatus(1);
                   System.out.println("exit: numeric argument required: " + status);
                   return;
               }
            }
            System.exit(exitStatus);
        }
        catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.err.println(ex.getMessage());
        }
    }
}
