package ysh;


import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();
        String cmd = "echo yagiz erdem; cmd1 -ar1 | cmd2 -arg2 & set a = 10";
        Scanner scanner = new Scanner(cmd);
        scanner.scanAll();
        Parser parser = new Parser(scanner.tokens);
        List<Type.AstNode> nodes = parser.parse();


        int a = 10;
    }
}