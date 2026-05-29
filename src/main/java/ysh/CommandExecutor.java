package ysh;


import ysh.builtin.*;

import java.io.*;
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
                return ExecuteCd.execute(command, options);
            }
            case "echo" : {
                return ExecuteEcho.execute(command, options);
            }
            case "exit" : {
                return ExecuteExit.execute(command, options);
            }
            case "dir":
            case "ls": {
                return ExecuteDir.execute(command, options);
            }
            case "set": {
                return ExecuteSet.execute(command, options);
            }
            case "mkdir" : {
                return ExecuteMkdir.execute(command, options);
            }
        }
        return response;
    }

}
