package ysh;

import ysharp.treewalk.evaluator.Interpreter;

import java.nio.file.Path;
import java.util.HashMap;

public class Context {

    private static Context instance = null;

    public Path cwd;

    public Environment env;

    public Interpreter interpreter;

    public int exitStatus;


    private Context() {
        this.env = new Environment();
        this.interpreter = new Interpreter();
        this.cwd = Path.of(System.getProperty("user.dir"));
        this.exitStatus = 0;
    }

    public static Context getContext() {
        if(Context.instance == null) {
            Context.instance = new Context();
        }
        return Context.instance;
    }

    public void setExitStatus(int existStatus) {
        this.exitStatus = existStatus;
    }
    public static class Environment {
        HashMap<String, String> variables = new HashMap<>();

        public  Environment() {}
    }

}
