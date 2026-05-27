package ysh.natives;

import ysh.Core;
import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Callable;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

import java.util.List;

public class Run  extends Function.NativeFunction  implements Callable {

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter,
                                 List<Variable.Variant> arguments)
            throws YsharpException {

        requireArity(arguments, arity(), getFnName());
        String shellScript = requireString(arguments.getFirst(), getFnName(), 1);
        Core.ExecuteShellProgram(shellScript);

        return new Variable.Variant(null);
    }

    @Override
    public String getFnName() {
        return "run";
    }
}
