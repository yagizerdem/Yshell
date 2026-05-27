package ysh;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

        String program = "\"echo hi{t\"";

        Core.ExecuteShellProgram(program);

    }
}