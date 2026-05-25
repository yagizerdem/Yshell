package ysh;

import ysharp.treewalk.YsharpException;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Type.Token> tokens;
    public int cursor;

    public Parser(List<Type.Token> tokens) {
        this.tokens = tokens;
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
            consumeWordBreak();

            while (!isEnd() && isSeparator(peek().type)) {
                advance(); // consume ; or & or newline
                consumeWordBreak();

                if (!(peek().type == Type.TokenType.LEFT_PAREN || isWordStart(peek().type))) {
                    break;
                }

                Type.AstNode command_ = parseConditional();
                consumeWordBreak();
                commands.add(command_);
            }

            if(isSeparator(peek().type)) advance();
            consumeWordBreak();

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
            consumeWordBreak();
            Type.AstNode pipelineNode_ = parsePipeline();
            consumeWordBreak();

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
            consumeWordBreak();
            Type.AstNode command_ = parseCommand();
            consumeWordBreak();

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
        List<Type.AstNode> elements = new ArrayList<>();

        elements.add(parseWord());
        consumeWordBreak();

        while (!isEnd() && !isCommandEnd(peek().type)) {
            if (isRedirectionStart(peek().type)) {
                elements.add(parseRedirection());
                consumeWordBreak();
            } else if (isWordStart(peek().type)) {
                elements.add(parseWord());
                consumeWordBreak();
            } else {
                break;
            }
        }

        return new Type.CommandNode(elements);
    }

    private Type.Word parseWord() {
        List<Type.WordPart> parts = new ArrayList<>();
        boolean hasTildeExpansion = false;
        if (!isWordStart(peek().type)) {
            throw new YsharpException(YsharpException.YsharpErrorType.SYNTAX,
                    -1,
                    "Expected word");
        }

        while (!isEnd() && isWordStart(peek().type)) {
            if(peek().type == Type.TokenType.PERCENT) {
                advance();
                parts.add(new Type.VariableWord(advance()));
                consume(Type.TokenType.PERCENT, "missing closing %");
                consumeWordBreak();
            }
            else if(peek().type == Type.TokenType.DOLLAR) {
                advance();
                if(match(Type.TokenType.LEFT_CURLY_BRACE)) {
                    parts.add(new Type.VariableWord(advance()));
                    consume(Type.TokenType.RIGHT_PAREN, "missing closing }");
                }
                else if(match(Type.TokenType.BACKTICK)) {
                    parts.add(new Type.ShellCommandWord(advance()));
                    consume(Type.TokenType.BACKTICK, "missing closing `");
                }
                consumeWordBreak();
            }
            else if(peek().type == Type.TokenType.DOUBLE_QUOTE) {
                List<Type.WordPart> wordParts = new ArrayList<>();
                advance();
                while (!isEnd() && peek().type != Type.TokenType.DOUBLE_QUOTE) {
                    if(peek().type == Type.TokenType.PERCENT) {
                        advance();
                        wordParts.add(new Type.VariableWord(advance()));
                        consume(Type.TokenType.PERCENT, "missing closing %");
                    }
                    else if(peek().type == Type.TokenType.DOLLAR) {
                        advance();
                        if(match(Type.TokenType.LEFT_CURLY_BRACE)) {
                            wordParts.add(new Type.VariableWord(advance()));
                            consume(Type.TokenType.RIGHT_PAREN, "missing closing }");
                        }
                        else if(match(Type.TokenType.BACKTICK)) {
                            wordParts.add(new Type.VariableWord(advance()));
                            consume(Type.TokenType.BACKTICK, "missing closing `");
                        }
                        consumeWordBreak();
                    }
                    else if(peek().type == Type.TokenType.SINGLE_QUOTE) {
                        advance();
                        wordParts.add(new Type.SinglequotedWord(advance()));
                        consume(Type.TokenType.SINGLE_QUOTE, "missing closing single quote \'");
                        consumeWordBreak();
                    }
                    else {
                        wordParts.add(new Type.UnquotedWord(advance()));
                    }
                }
                parts.add(new Type.DoublequotedWord(wordParts));
                consume(Type.TokenType.DOUBLE_QUOTE, "missing closing double quote \"");
                consumeWordBreak();
            }
            else if(peek().type == Type.TokenType.SINGLE_QUOTE) {
                advance();
                parts.add(new Type.SinglequotedWord(advance()));
                consume(Type.TokenType.SINGLE_QUOTE, "missing closing single quote \'");
                consumeWordBreak();
            }
            else if(peek().type == Type.TokenType.TILDE) {
                hasTildeExpansion = true;
            }
            else {
                // unquoted
                parts.add(new Type.UnquotedWord(advance()));
            }
        }

        return new Type.Word(parts, hasTildeExpansion);
    }


    private Type.AstNode parseRedirection() {
        Type.Token operator = advance();
        consumeWordBreak();

        if (isStreamRedirectionOperator(operator.type)) {
            return new Type.Redirection(operator, null);
        }

        Type.Word filename = parseWord();
        consumeWordBreak();

        return new Type.Redirection(operator, filename);
    }

    private Type.GroupedCommandNode parseGroupedCommand() {
        consume(Type.TokenType.LEFT_PAREN, "Expected '('");
        consumeWordBreak();

        List<Type.AstNode> commands = parseList();
        consumeWordBreak();

        consume(Type.TokenType.RIGHT_PAREN, "Expected ')' after grouped command");
        consumeWordBreak();

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
                type == Type.TokenType.PERCENT ||
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

    private void consumeWordBreak() {
        while (peek().type == Type.TokenType.WORD_BREAK) advance();
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
