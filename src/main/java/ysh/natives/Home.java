package ysh.natives;

import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

import java.nio.file.Paths;
import java.util.List;

public class Home extends Function.NativeFunction {

    @Override
    public int arity() {
        return 0;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter, List<Variable.Variant> arguments)
            throws YsharpException {

        requireArity(arguments, arity(), getFnName());

        return new Variable.Variant(
                Paths.get(System.getProperty("user.home"))
                        .toAbsolutePath()
                        .normalize()
                        .toString()
        );
    }

    @Override
    public String getFnName() {
        return "home";
    }
}