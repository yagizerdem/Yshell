package ysh.natives;

import ysh.CommandExecutor;
import ysh.Type;
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

public class Exec extends Function.NativeFunction  implements Callable {
    @Override
    public int arity() {
        return -1;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter,
                                 List<Variable.Variant> arguments)
            throws YsharpException {

        if(arguments.isEmpty()) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "at least 1 argument is required to run exec");
        }

        AtomicInteger i = new AtomicInteger(1);
        List<String> parts = arguments
                .stream().
                map(x -> requireString(x, getFnName(), i.getAndIncrement()))
                .toList();

        Type.Command shellCommand = new Type.Command();
        shellCommand.args = parts.stream().toList();
        // do not expand since this is native function, expansion only works in shell parser

        shellCommand.execute(new CommandExecutor());

        return new Variable.Variant(null);

    }

    @Override
    public String getFnName() {
        return "exec";
    }
}
