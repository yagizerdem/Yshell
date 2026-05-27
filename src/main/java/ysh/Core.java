package ysh;

import java.util.List;

public class Core {


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
