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
        AND_CONDITIONAL, // &&
        OR_CONDITIONAL, // ||
        PIPE, // |
        AND_SEPARATOR, // &
        DOUBLE_QUOTE,
        SINGLE_QUOTE,
        BACKTICK,
        SEMI_COLON, // ;
        DOLLAR, // $

        PERCENT, // %

        REDIRECT_OUT,          // >
        REDIRECT_OUT_APPEND,   // >>
        REDIRECT_IN,           // <
        REDIRECT_STDERR,          // 2>
        REDIRECT_STDERR_APPEND,   // 2>>
        REDIRECT_STDOUT,       // 1>
        REDIRECT_STDOUT_APPEND,// 1>>

        REDIRECT_STDERR_TO_STDOUT,   // 2>&1
        REDIRECT_STDOUT_TO_STDERR,   // 1>&2

        TEXT,

        NEWLINE, // \n

        LEFT_PAREN, // (
        RIGHT_PAREN, // )

        LEFT_CURLY_BRACE,  // {
        RIGHT_CURLY_BRACE, // }

        WORD_BREAK,

        TILDE, // ~
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

    static public interface AstNode { }

    public static final class  ConditionalNode implements AstNode {
        public final Type.AstNode first;
        public final List<ConditionalPart> rest;

        public ConditionalNode(Type.AstNode first, List<ConditionalPart> rest) {
            this.first = first;
            this.rest = rest;
        }

        final static class ConditionalPart {
            Token operator;
            Type.AstNode pipeline;

            public ConditionalPart() {}

            public ConditionalPart(Token operator, Type.AstNode pipeline) {
                this.operator = operator;
                this.pipeline = pipeline;
            }
        }
    }

    public static final class PipelineNode implements AstNode {
        public final Type.AstNode first;
        public final List<PipelineNode.PipelinePart> rest;

        public PipelineNode(Type.AstNode first, List<PipelineNode.PipelinePart> rest) {
            this.first = first;
            this.rest = rest;
        }

        public static class PipelinePart {
            Type.Token operator;
            Type.AstNode command;

            public PipelinePart() {}

            public PipelinePart(Token operator, Type.AstNode command) {
                this.operator = operator;
                this.command = command;
            }
        }
    }

    public static final class CommandNode implements AstNode {
        public List<CommandElement> commandElements;

        public CommandNode(List<CommandElement> commandElements) {
            this.commandElements = commandElements;
        }
    }

    static interface CommandElement {}

    public static final class Word implements CommandElement, AstNode {
        public List<Token> wordParts;

        public Word(List<Token> wordParts) {
            this.wordParts = wordParts;
        }
    }

    public static final class WordBreak implements CommandElement, AstNode { }

    public static final class Redirection implements CommandElement, AstNode {
        public Token redirection;

        public final Word filename; // may be null if redirection is std stream

        public Redirection(Token redirection, Word filename) {
            this.redirection = redirection;
            this.filename = filename;
        }
    }

    public static final class GroupedCommandNode implements AstNode {
        public final List<AstNode> list;
        public final List<AstNode> redirections;

        public GroupedCommandNode(List<AstNode> list, List<AstNode> redirections) {
            this.list = list;
            this.redirections = redirections;
        }
    }
}
