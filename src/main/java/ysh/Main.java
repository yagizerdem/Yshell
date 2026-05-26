package ysh;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

        Context ctx = Context.getContext();
        ctx.env.setVariable("abc" , "erdem");

        String cmd = "echo ~~yagi%abc%z $abc 'def' erd~em > %abc%";
        Scanner scanner = new Scanner(cmd);
        scanner.scanAll();
        Parser parser = new Parser(scanner.tokens);
        List<Type.AstNode> nodes = parser.parse();
        AstReducer.AstReducerVisitor reducer = new AstReducer.AstReducerVisitor(nodes);
        reducer.vectorizeAll();

        List<Type.BaseCommand> commands = reducer.vectorizedCommands;

        Expansion expansion = new Expansion();
        for(Type.BaseCommand command : commands) {
            command.variableSubstitution(expansion);
            command.tildeSubstitution(expansion);
        }

        int a = 10;

    }
}