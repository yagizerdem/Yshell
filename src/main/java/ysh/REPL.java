package ysh;

import ysh.natives.*;
import ysharp.treewalk.lexer.Lexer;
import ysharp.treewalk.lexer.Preprocess;
import ysharp.treewalk.lexer.Token;
import ysharp.treewalk.parser.Parser;

import java.util.List;

public class REPL {

    public static void start() throws Exception {
        String code = """
                   execRaw("ping google.com");
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

        Context context = Context.getContext();

        Register.nativeFunction(context.interpreter, new Run());
        Register.nativeFunction(context.interpreter, new Exec());
        Register.nativeFunction(context.interpreter, new ExecRaw());
        Register.nativeFunction(context.interpreter, new Pipe());
        Register.nativeFunction(context.interpreter, new Glob());
        Register.nativeFunction(context.interpreter, new Cwd());
        Register.nativeFunction(context.interpreter, new Home());
        Register.nativeFunction(context.interpreter, new Chdir());

        context.interpreter.interpret(program.program);

        if(context.interpreter.hadErrors()) {
            context.interpreter.errors.forEach(x -> {
                System.out.println(x.getMessage());
            });
        }


    }
}
