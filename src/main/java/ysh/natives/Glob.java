package ysh.natives;

import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Callable;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Native.Collections.Array.yArray;
import ysharp.treewalk.evaluator.Variable;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

public class Glob  extends Function.NativeFunction  implements Callable {
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

        try {

            Path root = Path.of(System.getProperty("user.dir"));
            PathMatcher matcher = FileSystems.getDefault()
                    .getPathMatcher("glob:" + pattern);

            ArrayList<Variable.Variant> paths = new ArrayList<>();

            try (var stream = Files.walk(root)) {
                stream
                        .filter(Files::isRegularFile)
                        .filter(path -> matcher.matches(root.relativize(path)))
                        .forEach(path -> paths.add(new Variable.Variant(path.toString())));
            }

            return new Variable.Variant(new yArray.yArrayInstance(paths));

        }catch (IOException ex) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , ex.getMessage());
        }

    }

    @Override
    public String getFnName() {
        return "glob";
    }
}
