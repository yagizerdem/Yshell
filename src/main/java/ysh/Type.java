package ysh;

import java.util.List;

public class Type {


    static public abstract class Line<T> {
        public int lineNo;

        public Line() {}

        public Line(int lineNo) {
            this.lineNo = lineNo;
        }

        public abstract T execute();

    }

    static public class Command<T> extends Line {
        public String rawCommand;
        public String expandedCommand;

        public Command() {}

        public Command(String rawCommand) {
            this.rawCommand = rawCommand;
        }

        public Command(String rawCommand, String expandedCommand) {
            this.rawCommand = rawCommand;
            this.expandedCommand = expandedCommand;
        }

        @Override
        public T execute() {
            return null;
        }
    }

    static public class Pipe<T> extends Line {
        public List<Command> commands;

        public Pipe() {}

        public Pipe(List<Command> commands) {
            this.commands = commands;
        }

        @Override
        public T execute() {
            return null;
        }
    }

}
