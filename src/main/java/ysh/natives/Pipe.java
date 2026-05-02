package ysh.natives;

import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Callable;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
                throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "at least 1 argument is required to run exec");
            }

            AtomicInteger i = new AtomicInteger(2);
            String arg = String.join(",",
                    arguments.stream().skip(1).map(x -> {
                        return requireString(x, getFnName(), i.getAndIncrement());
                    }).toList());

            String exeName = requireString(arguments.getFirst(), getFnName(), 1);

            ProcessBuilder pb =
                    new ProcessBuilder(exeName, arg);

            Process p = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            }

            p.waitFor();

            return new Variable.Variant(null);
        }catch (IOException ex) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1, ex.getMessage());
        }
        catch (InterruptedException ex) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1, ex.getMessage());
        }
    }

    @Override
    public String getFnName() {
        return "pipe";
    }
}
