package ysh;

import ysharp.treewalk.YsharpException;

import java.util.ArrayList;
import java.util.List;

public class Type {

    public interface Line {
        void execute(CommandExecutor executor);
    }

    static public class Command implements Line {
        public String rawCommand;
        public String expandedCommand;
        public List<String> args = new ArrayList<>(); // first one is exe name or built in , other params should be command line arguments
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
        public List<Command> commands = new ArrayList<>();

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

        public final ChainCommand chainCommand;

        public ChainCommand(Command command, Token operator, ChainCommand chainCommand) {
            this.command =command;
            this.operator = operator;
            this.chainCommand = chainCommand;
        }

        public ChainCommand(Command command, Token operator) {
            this.command =command;
            this.operator = operator;
            this.chainCommand = null;
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecuteChainCommand(this);
        }
    }

    static public char EOF = '\0';

    public static enum TokenType {
        ESCAPE, // ^
        AND_CONDITIONAL, // &&
        OR_CONDITIONAL, // ||
        PIPE, // |
        AND_SEPARATOR, // &
        UNQUOTED_WORD,
        SINGLE_QUOTED_WORD,
        DOUBLE_QUOTED_WORD,
        COMMAND_SUBSTITUTION_WORD,
        EXPANSION_WORD,
        SEMI_COLON, // ;

        REDIRECT_OUT,          // >
        REDIRECT_OUT_APPEND,   // >>
        REDIRECT_IN,           // <
        REDIRECT_STDERR,          // 2>
        REDIRECT_STDERR_APPEND,   // 2>>
        REDIRECT_STDOUT,       // 1>
        REDIRECT_STDOUT_APPEND,// 1>>

        REDIRECT_STDERR_TO_STDOUT,   // 2>&1
        REDIRECT_STDOUT_TO_STDERR,   // 1>&2

        NEWLINE, // \n
        EOF
    }

    static public class Token {
        public final String lexeme;
        public final TokenType type;

        public Token(String lexeme, TokenType type) {
            this.lexeme = lexeme;
            this.type = type;
        }
    }

}
