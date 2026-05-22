package ysh;

import ysharp.treewalk.YsharpException;

public class Cursor {
    public String src;
    public int cursor;

    public Cursor(String src) {
        this.src = src;
    }

    public char peek() {
        if(cursor >= this.src.length()) return Type.EOF;
        return this.src.charAt(cursor);
    }

    public char peekNext() {
        if(cursor + 1 >= this.src.length()) return Type.EOF;
        return this.src.charAt(cursor + 1);
    }

    public char advance() {
        if(cursor >= this.src.length()) return Type.EOF;
        char c = this.peek();
        cursor++;
        return c;
    }

    public char prev() {
        if(cursor == 0) throw new YsharpException(
                YsharpException.YsharpErrorType.PROCESS,
                -1,
                "Cannot move to previous character: cursor is already at the beginning of the source.");

        return this.src.charAt(cursor -1);
    }

    public boolean match(char expected) {
        if(cursor >= this.src.length()) return false;
        if(this.src.charAt(cursor) == expected) {
            this.cursor++;
            return  true;
        }
        else {
            return false;
        }
    }

    public boolean isEnd() {
        return cursor >= this.src.length();
    }

    public boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z');
    }

    public boolean isNumeric(char c) {
        return c >= '0' && c <= '9';
    }

    public boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isNumeric(c);
    }

    public boolean isAlphaOrUnderscore(char c) {
        return isAlpha(c) || c == '_';
    }

    public boolean isAlphaNumericOrUnderscore(char c) {
        return isAlphaNumeric(c) || c == '_';
    }
}
