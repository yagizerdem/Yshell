package ysh.natives;

import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Chdir extends Function.NativeFunction {

    @Override
    public int arity() {
        return 1;
    }

    @Override
    public Variable.Variant call(Interpreter interpreter, List<Variable.Variant> arguments)
            throws YsharpException {

        requireArity(arguments, arity(), getFnName());
        String pathText = requireString(arguments.getFirst(), getFnName(), 1);

        Path target;

        if (pathText.equals("~")) {
            target = Paths.get(System.getProperty("user.home"));
        } else {
            target = Paths.get(pathText);
        }

        target = target.toAbsolutePath().normalize();

        if (!Files.exists(target)) {
            throw new YsharpException(
                    YsharpException.YsharpErrorType.PROCESS,
                    -1,
                    "chdir: path does not exist: " + target
            );
        }

        if (!Files.isDirectory(target)) {
            throw new YsharpException(
                    YsharpException.YsharpErrorType.PROCESS,
                    -1,
                    "chdir: not a directory: " + target
            );
        }

        System.setProperty("user.dir", target.toString());

        return new Variable.Variant(target.toString());
    }

    @Override
    public String getFnName() {
        return "chdir";
    }
}