package ysh;


import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

        Type.Command cmd1 = new Type.Command();
        cmd1.args.addAll(List.of("git", "help"));

        Type.Command cmd2 = new Type.Command();
        cmd2.args.addAll(List.of("git", "-v"));

        Type.Command cmd3 = new Type.Command();
        cmd3.args.addAll(List.of("java", "-version"));

        Type.Token andToken = new Type.Token("&&", Type.TokenType.AND_CONDITIONAL);

        Type.ChainCommand chain =
                new Type.ChainCommand(
                        cmd1,
                        andToken,
                        new Type.ChainCommand(
                                cmd2,
                                andToken,
                                new Type.ChainCommand(cmd3, null, null)
                        )
                );

        CommandExecutor executor = new CommandExecutor();
        executor.ExecuteChainCommand(chain);
    }
}