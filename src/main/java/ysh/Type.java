package ysh;

import ysharp.treewalk.YsharpException;

import java.util.ArrayList;
import java.util.List;

public class Type {

    public interface BaseCommand {
        void execute(CommandExecutor executor);

        void variableSubstitution(Expansion expansion);

        void tildeSubstitution(Expansion expansion);

        void resolve(CommandResolver resolver);
    }

    static public class Command implements BaseCommand {

        public List<Word> rawArgs;
        public List<String> args = new ArrayList<>(); // first one is exe name or built in , other params should be command line arguments

        public List<Redirection> redirections = new ArrayList<>();
        public boolean isBuiltIn;

        public Command() {
            this.isBuiltIn = false;
            this.rawArgs = new ArrayList<>();
        }

        public Command(List<Word> rawArgs) {
            this.rawArgs = rawArgs;
            this.isBuiltIn = false;
        }

        public Command(List<Word> rawArgs, boolean isBuiltIn) {
            this.rawArgs = rawArgs;
            this.isBuiltIn = isBuiltIn;
        }

        public Command(List<Word> rawArgs, List<String> args) {
            this.rawArgs = rawArgs;
            this.args = args;
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecuteCommand(this);
        }

        @Override
        public void variableSubstitution(Expansion expansion) {
            expansion.VariableSubstitution(this);
        }

        @Override
        public void tildeSubstitution(Expansion expansion) {
            expansion.TildeSubstitution(this);
        }

        @Override
        public void resolve(CommandResolver resolver) {
            resolver.Resolve(this);
        }
    }

    static public class Pipe implements BaseCommand {
        public List<BaseCommand> commands = new ArrayList<>();

        public Pipe() {}

        public Pipe(List<BaseCommand> commands) {
            this.commands = commands;
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecutePipe(this);
        }

        @Override
        public void variableSubstitution(Expansion expansion) {
            for(BaseCommand command : this.commands) {
                command.variableSubstitution(expansion);
            }
        }

        @Override
        public void tildeSubstitution(Expansion expansion) {
            for(BaseCommand command : this.commands) {
                command.tildeSubstitution(expansion);
            }
        }

        @Override
        public void resolve(CommandResolver resolver) {
            for(BaseCommand command : this.commands) {
                command.resolve(resolver);
            }
        }
    }

    static public class ConditionalCommand implements BaseCommand {
        public BaseCommand command;
        public Token operator;

        public ConditionalCommand chainCommand;

        public ConditionalCommand() {};

        public ConditionalCommand(BaseCommand command, Token operator, ConditionalCommand chainCommand) {
            this.command =command;
            this.operator = operator;
            this.chainCommand = chainCommand;
        }

        public ConditionalCommand(BaseCommand command, Token operator) {
            this.command =command;
            this.operator = operator;
            this.chainCommand = null;
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecuteConditionalCommand(this);
        }

        @Override
        public void variableSubstitution(Expansion expansion) {
            command.variableSubstitution(expansion);
            ConditionalCommand cur = this.chainCommand;
            while (cur != null && cur.command != null) {
                cur.variableSubstitution(expansion);
                cur = cur.chainCommand;
            }
        }

        @Override
        public void tildeSubstitution(Expansion expansion) {
            command.tildeSubstitution(expansion);
            ConditionalCommand cur = this.chainCommand;
            while (cur != null && cur.command != null) {
                cur.tildeSubstitution(expansion);
                cur = cur.chainCommand;
            }
        }

        @Override
        public void resolve(CommandResolver resolver) {
            command.resolve(resolver);
            ConditionalCommand cur = this.chainCommand;
            while (cur != null && cur.command != null) {
                cur.resolve(resolver);
                cur = cur.chainCommand;
            }
        }
    }

    static public class GroupedCommand implements BaseCommand {
        public List<BaseCommand> commands;

        public List<AstNode> redirections = new ArrayList<>();

        public GroupedCommand() {
            this.commands = new ArrayList<>();
        }

        @Override
        public void execute(CommandExecutor executor) throws YsharpException {
            executor.ExecuteGroupedCommand(this);
        }

        @Override
        public void variableSubstitution(Expansion expansion) {
            for(BaseCommand command : this.commands) {
                command.variableSubstitution(expansion);
            }
        }

        @Override
        public void tildeSubstitution(Expansion expansion) {
            for(BaseCommand command : this.commands) {
                command.tildeSubstitution(expansion);
            }
        }

        @Override
        public void resolve(CommandResolver resolver) {
            for(BaseCommand command : this.commands) {
                command.resolve(resolver);
            }
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
        public String lexeme;
        public final TokenType type;

        public Token(String lexeme, TokenType type) {
            this.lexeme = lexeme;
            this.type = type;
        }
    }

    static public final class Pchar{
        char c;
        boolean isEscaped;

        public Pchar() {}

        public Pchar(char c, boolean isEscaped) {
            this.c = c;
            this.isEscaped = isEscaped;
        }

        @Override
        public String toString() {
            return String.valueOf(this.c);
        }
    }

    static public interface AstNode {
        <R> R accept(Visitor<R> visitor);
    }

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

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitConditionalNode(this);
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

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPipelineNode(this);
        }
    }

    public static final class CommandNode implements AstNode {
        public List<AstNode> commandElements;

        public CommandNode(List<AstNode> commandElements) {
            this.commandElements = commandElements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCommandNode(this);
        }
    }

    static interface CommandElement {}

    public static final class Word implements AstNode, CommandElement {
        public List<WordPart> parts;

        public boolean hasTildeExpansion;

        public Word() {
            this.parts = new ArrayList<>();
        }

        public Word(boolean hasTildeExpansion) {
            this.parts = new ArrayList<>();
            this.hasTildeExpansion = hasTildeExpansion;
        }

        public Word(List<WordPart> parts, boolean hasTildeExpansion) {
            this.parts = parts;
            this.hasTildeExpansion = hasTildeExpansion;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWord(this);
        }
    }

    public interface WordPart  {
        <R> R accept(Visitor<R> visitor);
    }

    public static final class UnquotedWord implements WordPart {
        public Token word;

        public UnquotedWord(Token word) {
            this.word = word;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnquotedWord(this);
        }
    }

    public static final class SinglequotedWord implements WordPart {
        public Token word;

        public SinglequotedWord(Token word) {
            this.word = word;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSinglequotedWord(this);
        }
    }

    public static final class DoublequotedWord implements WordPart {
        public List<WordPart> wordParts;

        public DoublequotedWord(List<WordPart> wordParts) {
            this.wordParts = wordParts;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitDoublequotedWord(this);
        }
    }


    public static final class ShellCommandWord implements WordPart {
        public Token word;

        public ShellCommandWord(Token word) {
            this.word = word;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitShellCommandWord(this);
        }
    }

    public static final class VariableWord implements WordPart {
        public Token word;

        public VariableWord(Token word) {
            this.word = word;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableWord(this);
        }
    }

//    public static final class WordBreak implements AstNode, CommandElement{
//        @Override
//        public <R> R accept(Visitor<R> visitor) {
//            return visitor.visitWordBreak(this);
//        }
//    }

    public static final class Redirection implements AstNode, CommandElement {
        public Token redirection;

        public final Word filename; // may be null if redirection is std stream

        public Redirection(Token redirection, Word filename) {
            this.redirection = redirection;
            this.filename = filename;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitRedirection(this);
        }
    }

    public static final class GroupedCommandNode implements AstNode {
        public final List<AstNode> list;
        public final List<AstNode> redirections;

        public GroupedCommandNode(List<AstNode> list, List<AstNode> redirections) {
            this.list = list;
            this.redirections = redirections;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupedCommandNode(this);
        }
    }
}
