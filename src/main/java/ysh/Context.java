package ysh;

import ysharp.treewalk.evaluator.Interpreter;

import java.nio.file.Path;
import java.util.HashMap;

public class Context {

    private static Context root = null;

    public static Context active;
    public Path cwd;

    public Environment env;

    public Interpreter interpreter;

    public Context enclosing;

    public int exitStatus;

    public ShellSettings settings;

    private Context() {
        this.env = new Environment();
        this.interpreter = new Interpreter();
        this.cwd = Path.of(System.getProperty("user.dir"));
        this.exitStatus = 0;
        this.settings = ShellSettings.defaults();
    }

    public static Context deepCopy() {
        Context ctx = new Context();
        return ctx;
    }

    public static Context getScoped() {
        Context ctx = Context.deepCopy();
        ctx.enclosing = Context.getContext();
        return ctx;
    }

    public Context getParent() {
        return this.enclosing;
    }

    public static Context getContext() {
        if(Context.root == null) {
            Context.root = new Context();
        }

        if(Context.active != null) {
            return Context.active;
        }

        return Context.root;
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

        public boolean hasVariableRecursive(String identifier) {
            if (variables.containsKey(identifier)) return true;
            Context ctx = Context.getContext();
            if (ctx.enclosing != null) {
                return ctx.enclosing.env.hasVariableRecursive(identifier);
            }

            return false;
        }

        public void setVariableIfNotExists(String identifier, String value) {
            if (variables.containsKey(identifier)) {
                throw new RuntimeException(
                        "Variable already exists: " + identifier
                );
            }
            variables.put(identifier, value);
        }

        public String getVariableRecursiveOrDefault(String identifier) {
            return getVariableRecursiveOrDefault(identifier, null);
        }

        public String getVariableRecursiveOrDefault(String identifier, String defaultVal) {
            if (variables.containsKey(identifier)) {
                return variables.get(identifier);
            }
            Context ctx = Context.getContext();
            if (ctx.enclosing != null) {
                return ctx.enclosing.env.getVariableRecursiveOrDefault(identifier, defaultVal);
            }

            return defaultVal;
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

    public static final class ShellSettings {
        public boolean interactive = false;
        public boolean echoCommands = false;
        public boolean stopOnError = false;

        public boolean captureStdout = false;
        public boolean captureStderr = false;
        public boolean mergeStderrToStdout = false;
        public boolean printOutput = true;

        public static ShellSettings defaults() {
            return new ShellSettings();
        }
    }
}
