package ysh.natives;

import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Callable;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

import java.util.List;

public class Globber extends Function.NativeFunction  implements Callable {
    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter,
                                 List<Variable.Variant> arguments)
            throws YsharpException {

        requireArity(arguments, arity(), getFnName());
        String pattern = requireString(arguments.getFirst(), getFnName(), 1);
        String cwd = System.getProperty("user.dir");


        return new Variable.Variant(null);
    }

    @Override
    public String getFnName() {
        return "glob";
    }
}
