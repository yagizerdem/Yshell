package ysh;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

        String program = "set a='\t' & echo **/*.java";

        Core.ExecuteShellProgram(program);

        // var b = Globber.expandGlob("**/*.java");
        // var a = 10;

    }
}