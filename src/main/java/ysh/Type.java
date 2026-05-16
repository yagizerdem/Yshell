package ysh;

import ysharp.treewalk.YsharpException;

import java.util.List;

public class Type {


    static public abstract class Line {

        public Line() {}

        public abstract void execute(CommandExecutor executor);

    }

    static public class Command extends Line {
        public String rawCommand;
        public String expandedCommand;
        public String exeName;
        public List<String> args;

        public Command() {}

        public Command(String rawCommand) {
            this.rawCommand = rawCommand;
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

    static public class Pipe extends Line {
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

}
