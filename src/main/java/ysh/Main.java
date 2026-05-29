package ysh;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        String program = """
                  echo **/*.java
                
                """;

        Core.ExecuteShellProgram(program);

    }
}