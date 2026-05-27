package ysh;

import ysh.natives.*;
import ysharp.treewalk.lexer.Cursor;
import ysharp.treewalk.lexer.Lexer;
import ysharp.treewalk.lexer.Token;

import java.util.List;

public class Core {

    public static void ExecuteYsharp(String shellScript) {

        try {
            List<Cursor.Pchar> preprocessed = ysharp.treewalk.lexer.Preprocess.removeComments(ysharp.treewalk.lexer.Preprocess.mergeContinuation(shellScript));

            Lexer lexer = new Lexer(preprocessed);
            List<Token> tokenStream = lexer.scanTokens();

            if(lexer.hadErrors()) {
                lexer.errors.forEach(x -> {
                    System.out.println(x.getMessage());
                });
            }

            ysharp.treewalk.parser.Parser parser = new ysharp.treewalk.parser.Parser(tokenStream);
            ysharp.treewalk.parser.Parser.Program program = parser.parse();

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
            Register.nativeFunction(context.interpreter, new GetExitStatusFn());

            context.interpreter.interpret(program.program);

            if(context.interpreter.hadErrors()) {
                context.interpreter.errors.forEach(x -> {
                    System.out.println(x.getMessage());
                });
            }
        }catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

    }

    public static Type.ProgramExecutionResponse ExecuteShellProgram(String program) {
        Context context = Context.getContext();
        return ExecuteShellProgram(program, context);
    }
    public static Type.ProgramExecutionResponse ExecuteShellProgram(String program, Context context) {
        Type.ProgramExecutionResponse response = new Type.ProgramExecutionResponse();
        response.stdOut = "";
        response.stdErr = "";
        try {
            Type.CommandExecutionOptions options = Type.CommandExecutionOptions.defaults();
            if(context.settings.captureStdout) {
                options = Type.CommandExecutionOptions.capture();
            }

            List<Type.Pchar> processed = Preprocess.preprocess(program);

            Scanner scanner = new Scanner(processed);
            scanner.scanAll();
            Parser parser = new Parser(scanner.tokens);
            List<Type.AstNode> nodes = parser.parse();
            AstReducer.AstReducerVisitor reducer = new AstReducer.AstReducerVisitor(nodes);
            reducer.vectorizeAll();

            List<Type.BaseCommand> commands = reducer.vectorizedCommands;

            Expansion expansion = new Expansion();
            CommandResolver commandResolver = new CommandResolver();
            CommandExecutor executor = new CommandExecutor();

            for(Type.BaseCommand command : commands) {
                // expansion
                command.variableSubstitution(expansion);
                command.tildeSubstitution(expansion);
                command.commandSubstitution(expansion);
                command.globSubstitution(expansion);

                // prepare command
                command.resolve(commandResolver);

                // execution
                Type.CommandExecutionResponse commandResponse = command.execute(executor, options);
                response.stdOut += commandResponse.stdOut;
                response.stdErr += commandResponse.stdErr;
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return response;
    }
}
