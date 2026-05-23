package ysh;

import ysharp.treewalk.YsharpException;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    public Cursor cursor;

    public int start;

    public boolean isEscaped;
    public final List<Type.Token> tokens = new ArrayList<>();

    public static enum State {
        IN_DOUBLE_QUOTE,
        IN_SINGLE_QUOTE,
        IN_BACKTICK,
        UNQUOTE,
    }

    private State state = State.UNQUOTE;

    public Scanner(String src)
    {
        this.cursor = new Cursor(src);
        this.start = 0;
        this.isEscaped = false;
    }

    private void addToken(Type.TokenType type) {
        this.tokens.add(new Type.Token(cursor.src.substring(start, cursor.cursor), type));
    }

    public void scanAll() {
        while (!this.cursor.isEnd()) {
            this.start = cursor.cursor;
            scan();
        }
        this.tokens.add(new Type.Token(String.valueOf(Type.EOF), Type.TokenType.EOF));
    }

    public void scan() {
        char c = this.cursor.advance();
        switch (c) {
            // single chars
            case ';': {
                if (!isEscaped) {
                    addToken(Type.TokenType.SEMI_COLON);
                    isEscaped = false;
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case '$': {
                if (!isEscaped) {
                    addToken(Type.TokenType.DOLLAR);
                    isEscaped = false;
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case '<': {
                if (!isEscaped) {
                    addToken(Type.TokenType.REDIRECT_IN);
                    isEscaped = false;
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case '^' : {
                if(!this.isEscaped) {
                    this.isEscaped = true;
                    break;
                }
                // collect unquoted word
                collectWord();
                break;
            }
            case '"' : {
                if(!isEscaped) {
                    state = State.IN_DOUBLE_QUOTE;
                    this.tokens.add(new Type.Token("\"", Type.TokenType.DOUBLE_QUOTE));
                    collectDoubleQuote();
                    this.tokens.add(new Type.Token("\"", Type.TokenType.DOUBLE_QUOTE)); // closing token
                    state = State.UNQUOTE;
                    break;
                }
                collectWord();
                break;
            }
            case '\'' : {
                if(!isEscaped) {
                    state = State.IN_SINGLE_QUOTE;
                    this.tokens.add(new Type.Token("\'", Type.TokenType.SINGLE_QUOTE));
                    collectSingleQuote();
                    this.tokens.add(new Type.Token("\'", Type.TokenType.SINGLE_QUOTE));
                    state = State.UNQUOTE;
                    isEscaped = false;
                    break;
                }

                isEscaped = false;
                collectWord();
                break;
            }
            case '`' : {
                if(!isEscaped) {
                    state = State.IN_BACKTICK;
                    this.tokens.add(new Type.Token("`", Type.TokenType.BACKTICK));
                    collectBackTick();
                    this.tokens.add(new Type.Token("`", Type.TokenType.BACKTICK));
                    state = State.UNQUOTE;
                    isEscaped = false;
                    break;
                }
                // collect word
                isEscaped = false;
                collectWord();
                break;
            }
            case '(': {
                if (!isEscaped) {
                    isEscaped = false;
                    addToken(Type.TokenType.LEFT_PAREN);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case ')': {
                if (!isEscaped) {
                    isEscaped = false;
                    addToken(Type.TokenType.RIGHT_PAREN);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case '%': {
                if (!isEscaped) {
                    isEscaped = false;
                    addToken(Type.TokenType.PERCENT);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case '{': {
                if (!isEscaped) {
                    isEscaped = false;
                    addToken(Type.TokenType.LEFT_CURLY_BRACE);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case '}': {
                if (!isEscaped) {
                    isEscaped = false;
                    addToken(Type.TokenType.RIGHT_CURLY_BRACE);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }

            // double chars
            case '&' : {
                if (!isEscaped) {
                    if (cursor.match('&')) {
                        addToken(Type.TokenType.AND_CONDITIONAL);
                        break;
                    }

                    addToken(Type.TokenType.AND_SEPARATOR);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            case '|': {
                if (!isEscaped) {
                    if (cursor.match('|')) {
                        addToken(Type.TokenType.OR_CONDITIONAL);
                        break;
                    }

                    addToken(Type.TokenType.PIPE);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }

            case '>': {
                if (!isEscaped) {
                    if (cursor.match('>')) {
                        addToken(Type.TokenType.REDIRECT_OUT_APPEND);
                        break;
                    }

                    addToken(Type.TokenType.REDIRECT_OUT);
                    break;
                }
                isEscaped = false;
                collectWord();
                break;
            }
            // quad
            case '1': {
                if (!isEscaped) {
                    if (cursor.match('>')) {
                        if (cursor.match('>')) {
                            addToken(Type.TokenType.REDIRECT_STDOUT_APPEND);
                            isEscaped = false;
                            break;
                        }

                        if (cursor.match('&')) {
                            if (cursor.match('2')) {
                                addToken(Type.TokenType.REDIRECT_STDOUT_TO_STDERR);
                                isEscaped = false;
                                break;
                            }
                            isEscaped = false;
                            collectWord();
                            break;
                        }

                        addToken(Type.TokenType.REDIRECT_STDOUT);
                        isEscaped = false;
                        break;
                    }
                }
                isEscaped = false;
                collectWord();
                break;
            }

            case '2': {
                if (!isEscaped) {
                    if (cursor.match('>')) {
                        if (cursor.match('>')) {
                            addToken(Type.TokenType.REDIRECT_STDERR_APPEND);
                            isEscaped = false;
                            break;
                        }

                        if (cursor.match('&')) {
                            if (cursor.match('1')) {
                                addToken(Type.TokenType.REDIRECT_STDERR_TO_STDOUT);
                                isEscaped = false;
                                break;
                            }

                            collectWord();
                            isEscaped = false;
                            break;
                        }

                        addToken(Type.TokenType.REDIRECT_STDERR);
                        isEscaped = false;
                        break;
                    }
                }

                isEscaped = false;
                collectWord();
                break;
            }
            default: {
                if(isBlank(c)) {
                    isEscaped = false;
                    break; // consume blank
                }
                if(c == '\n') {
                    isEscaped = false;
                    addToken(Type.TokenType.NEWLINE);
                    break;
                }

                isEscaped = false;
                collectWord();
            }
        }
    }

    public void collectWord() {
        String word = String.valueOf(cursor.prev());
        while (!cursor.isEnd() && ((!isWordBoundary(cursor.peek()) && !isEscaped) || isEscaped )) {
            char c = cursor.peek();
            if(c == '^') {
                if(this.isEscaped) {
                    word += c;
                    isEscaped = false;
                }
                else {
                    isEscaped = true;
                }
            }
            else {
                word += c;
                isEscaped = false;
            }
            // consume
            cursor.advance();
        }


        this.tokens.add(new Type.Token(word, Type.TokenType.TEXT));
    }

    public boolean isBlank(char c) {
        return c == ' ' || c == '\t';
    }

    public boolean isSpace(char c) {
        return  c == ' ' || c == '\t' || c == '\n';
    }

    public boolean isWordBoundary(char c) {
        if(state != State.IN_DOUBLE_QUOTE) {
            return isSpace(c)
                    || c == ';'
                    || c == '&'
                    || c == '|'
                    || c == '>'
                    || c == '<'
                    || c == '$'
                    || c == '"'
                    || c == '\''
                    || c == '`'
                    || c == '('
                    || c == ')'
                    || c == '{'
                    || c == '}'
                    || c == '%';
        }
        if(state == State.IN_DOUBLE_QUOTE) {
            return isSpace(c) || c == '\"';
        }

        return false;
    }

    public void collectSingleQuote() {
        StringBuilder word = new StringBuilder();

        boolean hasClosed = false;

        while (!cursor.isEnd()) {
            char c = cursor.peek();

            if (c == '\'' && !isEscaped) {
                hasClosed = true;
                cursor.advance();
                break;
            }

            if (c == '^') {
                if (this.isEscaped) {
                    word.append(c);
                    isEscaped = false;
                } else {
                    isEscaped = true;
                }
            } else {
                word.append(c);
                isEscaped = false;
            }

            cursor.advance();
        }

        if (!hasClosed) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 ,"Unclosed single quote");
        }

        this.tokens.add(new Type.Token(word.toString(), Type.TokenType.TEXT));
    }
    public void collectBackTick() {
        StringBuilder word = new StringBuilder();

        boolean hasClosed = false;

        while (!cursor.isEnd()) {
            char c = cursor.peek();

            if (c == '`' && !isEscaped) {
                hasClosed = true;
                cursor.advance();
                break;
            }

            if (c == '^') {
                if (this.isEscaped) {
                    word.append(c);
                    isEscaped = false;
                } else {
                    isEscaped = true;
                }
            } else {
                word.append(c);
                isEscaped = false;
            }

            cursor.advance();
        }

        if (!hasClosed) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 ,"Unclosed backtick");
        }

        this.tokens.add(new Type.Token(word.toString(), Type.TokenType.TEXT));
    }

    public void collectDoubleQuote() {
        List<Type.Token> tokens = new ArrayList<>();
        StringBuilder lexeme = new StringBuilder();
        boolean hasClosed = false;
        while (!cursor.isEnd()) {
            char c = cursor.peek();

            if (c == '"' && !isEscaped) {
                hasClosed = true;
                cursor.advance();
                break;
            }

            if (c == '^') {
                if (this.isEscaped) {
                    lexeme.append(c);
                    isEscaped = false;
                } else {
                    isEscaped = true;
                }
                cursor.advance();
                continue;
            }

            if(isSpace(c)) {
                if (!lexeme.isEmpty()) {
                    tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT));
                    lexeme.setLength(0);
                }

                // consume all blank as one token
                while (!cursor.isEnd() && isSpace(cursor.peek())) {
                    lexeme.append(cursor.advance());
                }

                tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT));
                lexeme.setLength(0);
                continue;
            }

            Type.TokenType type = switch (c) {
                case '$' -> Type.TokenType.DOLLAR;
                case '{' -> Type.TokenType.LEFT_CURLY_BRACE;
                case '}' -> Type.TokenType.RIGHT_CURLY_BRACE;
                case '%' -> Type.TokenType.PERCENT;
                case '`' -> Type.TokenType.BACKTICK;
                default -> null;
            };

            if (type != null) {
                if (!lexeme.isEmpty()) {
                    tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT));
                    lexeme.setLength(0);
                }

                tokens.add(new Type.Token(String.valueOf(c), type));
                cursor.advance();
                continue;
            }

            lexeme.append(c);
            cursor.advance();
        }

        if (!lexeme.isEmpty()) {
            tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT));
            lexeme.setLength(0);
        }

        if(!hasClosed) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 ,"Unclosed double quote");
        }

        this.tokens.addAll(tokens);
    }
}
