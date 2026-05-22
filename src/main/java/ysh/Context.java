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
        private HashMap<String, String> variables = new HashMap<>();

        public  Environment() {}

        public void setVariable(String identifier, String value) {
            variables.put(identifier, value);
        }

        public void setVariableIfNotExists(String identifier, String value) {
            if (variables.containsKey(identifier)) {
                throw new RuntimeException(
                        "Variable already exists: " + identifier
                );
            }
            variables.put(identifier, value);
        }

        public String getVariableOrDefault(String identifier) {
            if(variables.containsKey(identifier)) return variables.get(identifier);
            return null;
        }

        public String getVariableOrDefault(String identifier, String defaultVal) {
            if(variables.containsKey(identifier)) return variables.get(identifier);
            return defaultVal;
        }
    }

}
