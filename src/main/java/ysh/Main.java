package ysh;


import org.apache.hadoop.thirdparty.org.checkerframework.checker.units.qual.A;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();
        String cmd = "echo yagiz'erdem'\"sudenaz  $`echo yetkin`\" >> file.txt ; echo test > file2.txt & ((cmd1 && cmd2) | cmd3) >> out.txt  ";
        Scanner scanner = new Scanner(cmd);
        scanner.scanAll();
        Parser parser = new Parser(scanner.tokens);
        List<Type.AstNode> nodes = parser.parse();
        AstReducer.AstReducerVisitor reducer = new AstReducer.AstReducerVisitor(nodes);
        reducer.vectorizeAll();

        List<Type.BaseCommand> commands = reducer.vectorizedCommands;


        int a = 10;
    }
}