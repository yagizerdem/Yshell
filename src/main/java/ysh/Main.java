package ysh;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();

        String program = "set abc='fish fucker' & echo ~~yagi%abc%z$`echo sudenaz$`echo yetkin`` $abc 'def' erd~em > %abc%  " +
                "|| echo hit ; echo ~~yagi%abc%z " +
                "$abc 'def' erd~em > %abc%";

        Core.ExecuteShellProgram(program);

    }
}