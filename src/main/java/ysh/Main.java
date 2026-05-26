package ysh;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

       try {
           Context ctx = Context.getContext();

           String cmd = "set abc='fish fucker' & echo ~~yagi%abc%z $abc 'def' erd~em > %abc%";
           List<Type.Pchar> processed = Preprocess.preprocess(cmd);

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

           int a = 10;
       } catch (Exception ex) {
           System.out.println(ex.getMessage());
       }

    }
}