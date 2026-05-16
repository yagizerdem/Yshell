package ysh;

import ysh.natives.*;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.lexer.Lexer;
import ysharp.treewalk.lexer.Preprocess;
import ysharp.treewalk.lexer.Token;
import ysharp.treewalk.parser.Parser;

import java.util.List;

public class REPL {

    public static void start() throws Exception {
        String code = """
                   var list = glob("**/*.ysh");
                   println list.size();
                   println list.toString();
                """;

        Lexer lexer = new Lexer(Preprocess.removeComments(Preprocess.mergeContinuation(code)));
        List<Token> tokenStream = lexer.scanTokens();

        if(lexer.hadErrors()) {
            lexer.errors.forEach(x -> {
                System.out.println(x.getMessage());
            });
        }

        Parser parser = new Parser(tokenStream);
        Parser.Program program = parser.parse();

        if(parser.hadErrors()) {
            parser.errors.forEach(x -> {
                System.out.println(x.getMessage());
            });
        }

        Interpreter interpreter = new Interpreter();

        Register.nativeFunction(interpreter, new Run());
        Register.nativeFunction(interpreter, new Exec());
        Register.nativeFunction(interpreter, new ExecRaw());
        Register.nativeFunction(interpreter, new Pipe());
        Register.nativeFunction(interpreter, new Glob());

        interpreter.interpret(program.program);

        if(interpreter.hadErrors()) {
            interpreter.errors.forEach(x -> {
                System.out.println(x.getMessage());
            });
        }
    }
}
