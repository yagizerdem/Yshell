package ysh;


import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

        Type.Command cmd1 = new Type.Command();
        cmd1.isBuiltIn = true;
        cmd1.args.addAll(List.of("exit", "0"));

        CommandExecutor executor = new CommandExecutor();


        executor.ExecuteCommand(cmd1);

        while (true) {
            System.out.println("hit");
        }

    }
}