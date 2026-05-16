package ysh;


import ysharp.treewalk.YsharpException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {

    public void ExecuteCommand(Type.Command command) throws YsharpException {
        try {
            List<String> commandParts = new ArrayList<>();
            commandParts.add(command.exeName);
            commandParts.addAll(command.args);

            ProcessBuilder pb =
                    new ProcessBuilder(commandParts);

            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            p.waitFor();
        }catch (Exception ex) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 ,ex.getMessage());
        }
    }

    public void ExecutePipe(Type.Pipe pipe)  throws YsharpException {

        try{
            List<Thread> threads = new ArrayList<>();
            List<Process> processes = new ArrayList<>();

            for (int i = 0; i < pipe.commands.size(); i++) {
                Type.Command shellCommand = pipe.commands.get(i);
                List<String> commandParts = new ArrayList<>();
                commandParts.add(shellCommand.exeName);
                commandParts.addAll(shellCommand.args);

                ProcessBuilder pb =
                        new ProcessBuilder(commandParts);

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

                } catch (IOException e) {

                }
            }));

            for (Thread th : threads) {
                th.start();
            }

            for (Thread th : threads) {
                th.join();
            }
        }catch (Exception ex) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1, ex.getMessage());
        }
    }

}
