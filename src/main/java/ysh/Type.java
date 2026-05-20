package ysh;

import ysharp.treewalk.YsharpException;

import java.util.List;

public class Type {

    public interface Line {
        void execute(CommandExecutor executor);
    }

    static public class Command implements Line {
        public String rawCommand;
        public String expandedCommand;
        public List<String> args; // first one is exe name or built in , other params should be command line arguments
        public boolean isBuiltIn;

        public Command() {
            this.isBuiltIn = false;
        }

        public Command(String rawCommand) {
            this.rawCommand = rawCommand;
            this.isBuiltIn = false;
        }

        public Command(String rawCommand, boolean isBuiltIn) {
            this.rawCommand = rawCommand;
            this.isBuiltIn = isBuiltIn;
        }

        public Command(String rawCommand, String expandedCommand) {
            this.rawCommand = rawCommand;
            this.expandedCommand = expandedCommand;
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecuteCommand(this);
        }
    }

    static public class Pipe implements Line {
        public List<Command> commands;

        public Pipe() {}

        public Pipe(List<Command> commands) {
            this.commands = commands;
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecutePipe(this);
        }
    }

    static public class ChainCommand implements Line {
        public final Command command;
        public final Token operator;

        public ChainCommand(Command command, Token operator) {
            this.command =command;
            this.operator = operator;
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecuteChainCommand(this);
        }
    }

    public static enum TokenType {

    }

    static public class Token {

    }
}
