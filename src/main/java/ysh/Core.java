package ysh;

import java.util.List;

public class Core {

    public static void ExecuteShellProgram(String program) {
        try {
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

                // prepare command
                command.resolve(commandResolver);

                // execution
                command.execute(executor);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
