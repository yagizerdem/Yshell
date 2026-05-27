package ysh;

import ysharp.treewalk.YsharpException;

import java.util.ArrayList;
import java.util.List;

public class Scanner {

    public Cursor cursor;

    public int start;

    public final List<Type.Token> tokens = new ArrayList<>();

    public static enum State {
        IN_DOUBLE_QUOTE,
        IN_SINGLE_QUOTE,
        IN_BACKTICK,
        UNQUOTE,
    }

    private State state = State.UNQUOTE;

    public Scanner(List<Type.Pchar> src)
    {
        this.cursor = new Cursor(src);
        this.start = 0;
    }

    private void addToken(Type.TokenType type) {
        StringBuilder lexeme = new StringBuilder();
        List<Type.Pchar> rawLexeme = new ArrayList<>();
        for (int i = start; i < cursor.cursor; i++) {
            lexeme.append(cursor.src.get(i).c);
            rawLexeme.add(cursor.src.get(i));
        }
        this.tokens.add(new Type.Token(lexeme.toString(), type, rawLexeme));
    }

    public void scanAll() {
        while (!this.cursor.isEnd()) {
            this.start = cursor.cursor;
            scan();
        }
        this.tokens.add(new Type.Token(String.valueOf(Type.EOF), Type.TokenType.EOF));
    }

    public void scan() {
        boolean isEscaped = this.cursor.peek().isEscaped;
        char c = this.cursor.advance().c;
        switch (c) {
            // single chars
            case ';': {
                if (!isEscaped) {
                    addToken(Type.TokenType.SEMI_COLON);
                    break;
                }
                collectWord();
                break;
            }
            case '~': {
                if (!isEscaped) {
                    addToken(Type.TokenType.TILDE);
                    break;
                }
                collectWord();
                break;
            }
            case '$': {
                if (!isEscaped) {
                    addToken(Type.TokenType.DOLLAR);
                    break;
                }
                collectWord();
                break;
            }
            case '<': {
                if (!isEscaped) {
                    addToken(Type.TokenType.REDIRECT_IN);
                    break;
                }
                collectWord();
                break;
            }
            case '^' : {
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
                collectWord();
                break;
            }
            case '(': {
                if (!isEscaped) {
                    addToken(Type.TokenType.LEFT_PAREN);
                    break;
                }
                collectWord();
                break;
            }
            case ')': {
                if (!isEscaped) {
                    addToken(Type.TokenType.RIGHT_PAREN);
                    break;
                }
                collectWord();
                break;
            }
            case '%': {
                if (!isEscaped) {
                    addToken(Type.TokenType.PERCENT);
                    break;
                }
                collectWord();
                break;
            }
            case '{': {
                if (!isEscaped) {
                    addToken(Type.TokenType.LEFT_CURLY_BRACE);
                    break;
                }
                collectWord();
                break;
            }
            case '}': {
                if (!isEscaped) {
                    addToken(Type.TokenType.RIGHT_CURLY_BRACE);
                    break;
                }
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
                                break;
                            }
                            collectWord();
                            break;
                        }

                        addToken(Type.TokenType.REDIRECT_STDOUT);
                        break;
                    }
                }
                collectWord();
                break;
            }

            case '2': {
                if (!isEscaped) {
                    if (cursor.match('>')) {
                        if (cursor.match('>')) {
                            addToken(Type.TokenType.REDIRECT_STDERR_APPEND);
                            break;
                        }

                        if (cursor.match('&')) {
                            if (cursor.match('1')) {
                                addToken(Type.TokenType.REDIRECT_STDERR_TO_STDOUT);
                                break;
                            }

                            collectWord();
                            break;
                        }

                        addToken(Type.TokenType.REDIRECT_STDERR);
                        break;
                    }
                }

                collectWord();
                break;
            }
            default: {
                if (isBlank(c)) {

                    while (!cursor.isEnd() && isBlank(cursor.peek())) {
                        cursor.advance();
                    }

                    if (!tokens.isEmpty() && tokens.get(tokens.size() - 1).type != Type.TokenType.WORD_BREAK) {
                        tokens.add(new Type.Token(" ", Type.TokenType.WORD_BREAK));
                    }

                    break;
                }
                if(c == '\n') {
                    addToken(Type.TokenType.NEWLINE);
                    break;
                }

                collectWord();
            }
        }
    }

    public void collectWord() {
        boolean isEscaped = this.cursor.peek().isEscaped;
        String word = String.valueOf(cursor.prev().c);
        List<Type.Pchar> rawLiteral = new ArrayList<>();
        rawLiteral.add(cursor.prev());
        while (!cursor.isEnd() && ((!isWordBoundary(cursor.cursor) && !isEscaped) || isEscaped )) {
            Type.Pchar pChar = cursor.peek();
            word += pChar.c;
            rawLiteral.add(pChar);
            // consume
            cursor.advance();
        }

        this.tokens.add(new Type.Token(word, Type.TokenType.TEXT, rawLiteral));
    }

    public boolean isBlank(char c) {
        return c == ' ' || c == '\t';
    }

    public boolean isSpace(char c) {
        return  c == ' ' || c == '\t' || c == '\n';
    }

    public boolean isWordBoundary(int cursorPosition) {
        Type.Pchar pChar = this.cursor.getCharOrDefault(cursorPosition, null);
        if(pChar == null || pChar.isEscaped) return false;
        switch (pChar.c) {
            case ';':
            case '&':
            case '|':
            case '>':
            case '<':
            case '$':
            case '"':
            case '\'':
            case '`':
            case '(':
            case ')':
            case '%':
                return true;

            case '{':
            case '}': {
                if(cursorPosition - 1 > 0) {
                    Type.Pchar prev = this.cursor.src.get(cursorPosition - 1);
                    if(prev.c == '$' && !prev.isEscaped) return true;
                }
                return false;
            }

            default:
                return isSpace(pChar);
        }
    }

    public boolean isBlank(Type.Pchar p) {
        return p != null && isBlank(p.c);
    }

    public boolean isSpace(Type.Pchar p) {
        return p != null && isSpace(p.c);
    }


    public void collectSingleQuote() {
        StringBuilder word = new StringBuilder();
        List<Type.Pchar> rawLiteral = new ArrayList<>();
        boolean hasClosed = false;

        while (!cursor.isEnd()) {
            Type.Pchar pChar = cursor.peek();

            if (pChar.c == '\'' && !pChar.isEscaped) {
                hasClosed = true;
                cursor.advance();
                break;
            }
            word.append(pChar);
            rawLiteral.add(pChar);
            cursor.advance();
        }

        if (!hasClosed) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 ,"Unclosed single quote");
        }

        this.tokens.add(new Type.Token(word.toString(), Type.TokenType.TEXT, rawLiteral));
    }
    public void collectBackTick() {
        int nestedCommandCounter = 0;
        StringBuilder word = new StringBuilder();
        List<Type.Pchar> rawLiteral = new ArrayList<>();
        boolean hasClosed = false;

        while (!cursor.isEnd()) {
            Type.Pchar pChar = cursor.peek();

            if (pChar.c == '`' && !pChar.isEscaped) {
                if(nestedCommandCounter == 0) {
                    hasClosed = true;
                    cursor.advance();
                    break;
                }
                else {
                    cursor.advance();
                    word.append("`");
                    rawLiteral.add(cursor.prev());
                    nestedCommandCounter--;
                    continue;
                }
            }

            if (pChar.c == '$' && !pChar.isEscaped) {
                cursor.advance();
                word.append("$");
                rawLiteral.add(cursor.prev());
                rawLiteral.add(pChar);

                if (!cursor.isEnd() && cursor.peek().c == '`') {
                    cursor.advance();
                    word.append("`");
                    rawLiteral.add(cursor.prev());
                    nestedCommandCounter++;
                }

                continue;
            }

            word.append(pChar);
            rawLiteral.add(pChar);

            cursor.advance();
        }

        if (!hasClosed) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 ,"Unclosed backtick");
        }

        this.tokens.add(new Type.Token(word.toString(), Type.TokenType.TEXT, rawLiteral));
    }

    public void collectDoubleQuote() {
        List<Type.Token> tokens = new ArrayList<>();
        StringBuilder lexeme = new StringBuilder();
        List<Type.Pchar> rawLiteral = new ArrayList<>();
        boolean hasClosed = false;
        while (!cursor.isEnd()) {
            Type.Pchar pChar = cursor.peek();

            if (pChar.c == '"' && !pChar.isEscaped) {
                hasClosed = true;
                cursor.advance();
                break;
            }

            if(isSpace(pChar)) {
                if (!lexeme.isEmpty()) {
                    tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT, rawLiteral));
                    lexeme.setLength(0);
                    rawLiteral = new ArrayList<>();
                }

                // consume all blank as one token
                while (!cursor.isEnd() && isSpace(cursor.peek())) {
                    Type.Pchar pChar_ = cursor.peek();
                    lexeme.append(pChar_.c);
                    rawLiteral.add(pChar_);
                    cursor.advance();
                }

                tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT, rawLiteral));
                lexeme.setLength(0);
                rawLiteral = new ArrayList<>();
                continue;
            }

            if(pChar.c == '$' && !pChar.isEscaped)  {
                tokens.add(new Type.Token("$", Type.TokenType.DOLLAR, List.of(new Type.Pchar('$', false))));
                cursor.advance();

                Type.TokenType type = switch (cursor.peek().c) {
                    case '{' -> Type.TokenType.LEFT_CURLY_BRACE;
                    case '}' -> Type.TokenType.RIGHT_CURLY_BRACE;
                    case '`' -> Type.TokenType.BACKTICK;
                    default -> null;
                };

                if (type != null) {
                    tokens.add(new Type.Token(String.valueOf(cursor.peek()), type,  List.of(cursor.peek())));
                    pChar =  cursor.advance();

                    // collect all variable/substitution body as atom
                    if (pChar.c == '`') {
                        while (!cursor.isEnd() && cursor.peek().c != '`') {
                            Type.Pchar pChar_ = cursor.peek();
                            lexeme.append(pChar_.c);
                            rawLiteral.add(pChar_);
                            cursor.advance();
                        }

                        if (!lexeme.isEmpty()) {
                            tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT, rawLiteral));
                            lexeme.setLength(0);
                            rawLiteral = new ArrayList<>();
                        }

                        Type.Pchar closing = cursor.advance();
                        if(closing.c != '`') {
                            throw new YsharpException(
                                    YsharpException.YsharpErrorType.PROCESS,
                                    -1,
                                    "Unclosed backtick");
                        }
                        tokens.add(new Type.Token(String.valueOf("`"), Type.TokenType.BACKTICK, List.of(new Type.Pchar('`', false))));

                    }
                    else if (pChar.c == '{') {
                        while (!cursor.isEnd() && cursor.peek().c != '}') {
                            Type.Pchar pChar_ = cursor.peek();
                            lexeme.append(pChar_.c);
                            rawLiteral.add(pChar_);
                            cursor.advance();
                        }

                        if (!lexeme.isEmpty()) {
                            tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT, rawLiteral));
                            lexeme.setLength(0);
                            rawLiteral = new ArrayList<>();
                        }

                        Type.Pchar closing = cursor.advance();
                        if(closing.c != '}') {
                            throw new YsharpException(
                                    YsharpException.YsharpErrorType.PROCESS,
                                    -1,
                                    "Unclosed curly brace");
                        }
                        tokens.add(new Type.Token(String.valueOf("}"), Type.TokenType.RIGHT_CURLY_BRACE, List.of(new Type.Pchar('{',  false))));

                    }

                    continue;
                }

                continue;
            }

            if(pChar.c == '%' && !pChar.isEscaped) {
                while (!cursor.isEnd() && cursor.peek().c != '%') {
                    Type.Pchar pChar_ = cursor.peek();
                    lexeme.append(pChar_.c);
                    rawLiteral.add(pChar_);
                    cursor.advance();
                }

                if (!lexeme.isEmpty()) {
                    tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT));
                    lexeme.setLength(0);
                    rawLiteral = new ArrayList<>();
                }

                Type.Pchar closing = cursor.advance();
                if(closing.c != '%') {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "Unclosed percent");
                }
                tokens.add(new Type.Token(String.valueOf("%"), Type.TokenType.PERCENT, List.of(new Type.Pchar('%', false))));

                continue;
            }

            if (pChar.c == '~') {
                if (pChar.isEscaped) {
                    lexeme.append(pChar.c);
                    rawLiteral.add(pChar);
                } else {
                    tokens.add(new Type.Token("~", Type.TokenType.TILDE, List.of(new Type.Pchar('~', false))));
                }

                cursor.advance();
                continue;
            }

            lexeme.append(pChar.c);
            rawLiteral.add(pChar);
            cursor.advance();
        }

        if (!lexeme.isEmpty()) {
            tokens.add(new Type.Token(lexeme.toString(), Type.TokenType.TEXT));
            lexeme.setLength(0);
            rawLiteral = new ArrayList<>();
        }

        if(!hasClosed) {
            throw new YsharpException(YsharpException.YsharpErrorType.PROCESS, -1 ,"Unclosed double quote");
        }

        this.tokens.addAll(tokens);
    }
}
