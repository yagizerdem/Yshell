package ysh.natives;

import ysh.CommandExecutor;
import ysh.Type;
import ysharp.treewalk.YsharpException;
import ysharp.treewalk.evaluator.Callable;
import ysharp.treewalk.evaluator.Function;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.evaluator.Variable;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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

        if(arguments.isEmpty()) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "at least 1 argument is required to run pipe");
        }

        Type.Pipe pipe = new Type.Pipe();
        pipe.commands = new ArrayList<>();

        for(Variable.Variant var : arguments) {
            if(!var.isString()) {
                throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "all arguments should be string");
            }

            String rawCommand = var.asString();

            List<String> parts = Arrays.stream(rawCommand.split("\\s+")).toList();
            if(parts.isEmpty()) {
                throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 , "at least 1 argument is required to run execRaw");
            }

            Type.Command shellCommand = new Type.Command();
            shellCommand.args = parts.stream().toList();

            pipe.commands.add(shellCommand);
        }

        pipe.execute(new CommandExecutor());

        return new Variable.Variant(null);
    }

    @Override
    public String getFnName() {
        return "pipe";
    }
}
