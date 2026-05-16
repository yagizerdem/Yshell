package ysh.natives;

import org.apache.hadoop.thirdparty.org.checkerframework.checker.units.qual.C;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecRaw extends Function.NativeFunction  implements Callable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter,
                                 List<Variable.Variant> arguments)
            throws YsharpException {

        requireArity(arguments, arity(), getFnName());
        String rawCommand = requireString(arguments.getFirst(), getFnName(), 1);

        List<String> parts = Arrays.stream(rawCommand.split("\\s+")).toList();
        if(parts.isEmpty()) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "at least 1 argument is required to run execRaw");
        }

        Type.Command shellCommand = new Type.Command();
        shellCommand.args = parts.stream().skip(1).toList();
        shellCommand.exeName = parts.getFirst();
        shellCommand.rawCommand = rawCommand;
        // do not expand since this is native function, expansion only works in shell parser

        shellCommand.execute(new CommandExecutor());

        return new Variable.Variant(null);
    }

    @Override
    public String getFnName() {
        return "execRaw";
    }
}
