package ysh;


import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

        Type.Command cmd1 = new Type.Command();
        cmd1.isBuiltIn = true;
        cmd1.args.addAll(List.of("echo", "yagiz    erdem"));

        CommandExecutor executor = new CommandExecutor();


        executor.ExecuteCommand(cmd1);

    }
}