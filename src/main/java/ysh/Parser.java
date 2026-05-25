package ysh;

import com.jcraft.jsch.MAC;
import ysharp.treewalk.YsharpException;

import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Type.Token> tokens;
    public int cursor;

    public Parser(List<Type.Token> tokens) {
        this.tokens = tokens.stream().filter(x -> x.type != Type.TokenType.WORD_BREAK).toList();
        this.cursor = 0;
    }

    private Type.Token peek() {
        return this.tokens.get(this.cursor);
    }

    private Type.Token peekNext() {
        if(peek().type == Type.TokenType.EOF) return peek();
        return this.tokens.get(this.cursor + 1);
    }

    private boolean isEnd() {
        return peek().type == Type.TokenType.EOF;
    }

    private Type.Token advance() {
        if (!isEnd()) {
            cursor++;
        }
        return tokens.get(cursor - 1);
    }

    private boolean match(Type.TokenType type) {
        if(isEnd() && type == Type.TokenType.EOF) return true;

        if(peek().type == type) {
            advance();
            return true;
        }
        return false;
    }

    private Type.Token consume(Type.TokenType type, String message) {
        Type.Token t = peek();
        if (!match(type)) {
            throw new YsharpException(YsharpException.YsharpErrorType.SYNTAX, -1, message);
        }
        return t;
    }

    public List<Type.AstNode> parse() {
        return parseList();
    }

    private List<Type.AstNode> parseList() {
        List<Type.AstNode> commands = new ArrayList<>();
        consumeToken(Type.TokenType.NEWLINE);

        if(isWordStart(peek().type) || peek().type == Type.TokenType.LEFT_PAREN) {
            Type.AstNode command = parseConditional();
            commands.add(command);

            while (!isEnd() && (
                    isSeparator(peek().type) &&
                            (peekNext().type == Type.TokenType.LEFT_PAREN || isWordStart(peekNext().type))
                    )) {
                    advance(); // consume separator
                    Type.AstNode command_ = parseConditional();
                    commands.add(command_);
            }

            if(isSeparator(peek().type)) advance();

        }

        consumeToken(Type.TokenType.NEWLINE);

        return commands;
    }

    private Type.AstNode parseConditional() {
        Type.AstNode pipelineNode = parsePipeline();

        List<Type.ConditionalNode.ConditionalPart> parts = new ArrayList<>();

        while (!isEnd() &&
                (check(Type.TokenType.AND_CONDITIONAL) || check(Type.TokenType.OR_CONDITIONAL))) {
            Type.Token op = advance();
            Type.AstNode pipelineNode_ = parsePipeline();

            parts.add(new Type.ConditionalNode.ConditionalPart(op, pipelineNode_));
        }

        if(parts.isEmpty()) parts = null;
        return new Type.ConditionalNode(pipelineNode, parts);
    }

    private Type.AstNode parsePipeline() {
        Type.AstNode command = parseCommand();

        List<Type.PipelineNode.PipelinePart> parts = new ArrayList<>();

        while (!isEnd() &&
                (check(Type.TokenType.PIPE))) {
            Type.Token pipeOp = advance();
            Type.AstNode command_ = parseCommand();

            parts.add(new Type.PipelineNode.PipelinePart(pipeOp, command_));
        }

        if(parts.isEmpty()) parts = null;
        return new Type.PipelineNode(command, parts);
    }

    private Type.AstNode parseCommand() {
        if (isWordStart(peek().type)) {
            return parseSimpleCommand();
        }

        if (peek().type == Type.TokenType.LEFT_PAREN) {
            return parseGroupedCommand();
        }

        throw new YsharpException(
                YsharpException.YsharpErrorType.SYNTAX,
                -1,
                "Expected command");
    }

    private Type.AstNode parseSimpleCommand() {
        List<Type.CommandElement> elements = new ArrayList<>();

        elements.add(parseWord());

        while (!isEnd() && !isCommandEnd(peek().type)) {
            if (isRedirectionStart(peek().type)) {
                elements.add(parseRedirection());
            } else if (isWordStart(peek().type)) {
                elements.add(parseWord());
            } else {
                break;
            }
        }

        return new Type.CommandNode(elements);
    }

    private Type.Word parseWord() {
        List<Type.Token> parts = new ArrayList<>();

        if (!isWordStart(peek().type)) {
            throw new YsharpException(YsharpException.YsharpErrorType.SYNTAX,
                    -1,
                    "Expected word");
        }

        while (!isEnd() && isWordPart(peek().type)) {
            parts.add(advance());
        }

        return new Type.Word(parts);
    }

    private Type.Redirection parseRedirection() {
        Type.Token operator = advance();

        if (isStreamRedirectionOperator(operator.type)) {
            return new Type.Redirection(operator, null);
        }

        Type.Word filename = parseWord();

        return new Type.Redirection(operator, filename);
    }

    private Type.GroupedCommandNode parseGroupedCommand() {
        consume(Type.TokenType.LEFT_PAREN, "Expected '('");

        List<Type.AstNode> commands = parseList();

        consume(Type.TokenType.RIGHT_PAREN, "Expected ')' after grouped command");

        List<Type.AstNode> redirections = new ArrayList<>();

        while (!isEnd()) {

            if (!isRedirectionStart(peek().type)) {
                break;
            }

            redirections.add(parseRedirection());
        }

        return new Type.GroupedCommandNode(commands, redirections);
    }
    private boolean check(Type.TokenType type) {
        return peek().type == type;
    }

    private boolean isWordStart(Type.TokenType type) {
        return type == Type.TokenType.SINGLE_QUOTE ||
                type == Type.TokenType.DOUBLE_QUOTE ||
                type == Type.TokenType.DOLLAR ||
                type == Type.TokenType.TEXT ||
                type == Type.TokenType.TILDE;
    }

    private boolean isSeparator(Type.TokenType type) {
        return type == Type.TokenType.AND_SEPARATOR ||
                type == Type.TokenType.SEMI_COLON ||
                type == Type.TokenType.NEWLINE;
    }

    private boolean isCommandEnd(Type.TokenType type) {
        return type == Type.TokenType.EOF
                || type == Type.TokenType.NEWLINE
                || type == Type.TokenType.SEMI_COLON
                || type == Type.TokenType.AND_SEPARATOR
                || type == Type.TokenType.PIPE
                || type == Type.TokenType.AND_CONDITIONAL
                || type == Type.TokenType.OR_CONDITIONAL
                || type == Type.TokenType.RIGHT_PAREN;
    }

    private boolean isRedirectionStart(Type.TokenType type) {
        return type == Type.TokenType.REDIRECT_OUT
                || type == Type.TokenType.REDIRECT_OUT_APPEND
                || type == Type.TokenType.REDIRECT_IN
                || type == Type.TokenType.REDIRECT_STDERR
                || type == Type.TokenType.REDIRECT_STDERR_APPEND
                || type == Type.TokenType.REDIRECT_STDOUT
                || type == Type.TokenType.REDIRECT_STDOUT_APPEND
                || type == Type.TokenType.REDIRECT_STDERR_TO_STDOUT
                || type == Type.TokenType.REDIRECT_STDOUT_APPEND;
    }

    private boolean isStreamRedirectionOperator(Type.TokenType type) {
        return type == Type.TokenType.REDIRECT_STDERR_TO_STDOUT
                || type == Type.TokenType.REDIRECT_STDOUT_APPEND;
    }

    private boolean isWordPart(Type.TokenType type) {
        return isWordStart(type) ||
                type == Type.TokenType.BACKTICK ||
                type == Type.TokenType.LEFT_CURLY_BRACE ||
                type == Type.TokenType.RIGHT_CURLY_BRACE;
    }

    public void consumeToken(Type.TokenType type) {
        while (!isEnd() && peek().type == type) advance();
    }

    public void consumeSeparator() {
        consumeToken(Type.TokenType.NEWLINE);
        consumeToken(Type.TokenType.SEMI_COLON);
        consumeToken(Type.TokenType.AND_SEPARATOR);
    }
}
