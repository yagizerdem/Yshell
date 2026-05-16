package ysh.natives;

import ysh.Context;
import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.*;

import java.util.List;

public class Cwd  extends Function.NativeFunction  implements Callable {

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter,
                                 List<Variable.Variant> arguments)
            throws YsharpException {

        requireArity(arguments, arity(), getFnName());
        Context context = Context.getContext();
        String cwd =  context.cwd.toString();

        return new Variable.Variant(new yString.yStringInstance(cwd));
    }

    @Override
    public String getFnName() {
        return "cwd";
    }
}