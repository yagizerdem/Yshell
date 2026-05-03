package ysh;


import ysh.natives.*;
import ysharp.treewalk.evaluator.Interpreter;
import ysharp.treewalk.lexer.Lexer;
import ysharp.treewalk.lexer.Preprocess;
import ysharp.treewalk.lexer.Token;
import ysharp.treewalk.parser.Parser;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        String code = """
                   pipe("where java", "findstr java");
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
        Register.nativeFunction(interpreter, new Globber());
        Register.nativeFunction(interpreter, new Exec());
        Register.nativeFunction(interpreter, new ExecRaw());
        Register.nativeFunction(interpreter, new Pipe());

        interpreter.interpret(program.program);

        if(interpreter.hadErrors()) {
            interpreter.errors.forEach(x -> {
                System.out.println(x.getMessage());
            });
        }



    }
}