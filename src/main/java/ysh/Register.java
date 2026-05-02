package ysh;

import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

public class Register {

    public static void nativeFunction(Interpreter interpreter, Function.NativeFunction function) throws Exception  {
        Variable.Variant variant = new Variable.Variant(function);
        Variable var = new Variable(variant, true, function.getType());
        interpreter.defineGlobal(function.getFnName(), var);
    }
}
