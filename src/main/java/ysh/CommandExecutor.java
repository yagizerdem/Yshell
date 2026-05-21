package ysh;


import ysharp.treewalk.YsharpException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {

    public void ExecuteCommand(Type.Command command) {
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
}
