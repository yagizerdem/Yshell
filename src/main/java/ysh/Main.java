package ysh;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        String program = """
                for var i = 0; i < 10; i++ do
                    println i;
                    run(\"""set a='\t' & echo **/*.java & echo 'test \n' ; echo "test \n test" \""");
                end
                """;

        Core.ExecuteYsharp(program);

    }
}