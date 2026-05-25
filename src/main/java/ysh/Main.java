package ysh;


import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        // REPL.start();
        String cmd = "echo yagiz'erdem'\"sudenaz  $`echo yetkin`\";";
        Scanner scanner = new Scanner(cmd);
        scanner.scanAll();
        Parser parser = new Parser(scanner.tokens);
        List<Type.AstNode> nodes = parser.parse();


        int a = 10;
    }
}