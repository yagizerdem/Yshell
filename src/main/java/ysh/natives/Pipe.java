package ysh.natives;

import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Callable;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Pipe extends Function.NativeFunction  implements Callable  {
    @Override
    public int arity() {
        return -1;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter,
                                 List<Variable.Variant> arguments)
            throws YsharpException {

        try {
            if(arguments.isEmpty()) {
                throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "at least 1 argument is required to run pipe");
            }

            for(Variable.Variant var : arguments) {
                if(!var.isString()) {
                    throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "all arguments should be string");
                }
            }

            List<Thread> threads = new ArrayList<>();
            List<Process> processes = new ArrayList<>();

            for(int i = 0; i < arguments.size(); i++) {
                String shellCommand = arguments.get(i).asString();
                ProcessBuilder pb =
                        new ProcessBuilder(shellCommand.split("\\s+"));

                processes.add(pb.
                        redirectOutput(ProcessBuilder.Redirect.PIPE)
                        .redirectError(ProcessBuilder.Redirect.PIPE)
                        .start());
            }


            for(int i = 0; i < processes.size() - 1; i++) {
                Process left = processes.get(i);
                Process right = processes.get(i + 1);

                threads.add(new Thread(()  -> {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(left.getInputStream()));
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(right.getOutputStream()));
                    String lineToPipe;

                    try {

                        while ((lineToPipe = bufferedReader.readLine()) != null){
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
                    while ((lineToPipe = bufferedReader.readLine()) != null){
                        System.out.println(lineToPipe);
                    }

                } catch (IOException e) {

                }
            }));

            for(Thread th : threads) {
                th.start();
            }

            for(Thread th: threads) {
                th.join();
            }


            return new Variable.Variant(null);

        }
        catch (Exception ex) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1, ex.getMessage());
        }
    }

    @Override
    public String getFnName() {
        return "pipe";
    }
}
