package ysh;

import ysharp.treewalk.YsharpException;

import java.util.List;

public class Cursor {
    public List<Type.Pchar> src;
    public int cursor;

    public Cursor(List<Type.Pchar> src) {
        this.src = src;
    }

    public Type.Pchar peek() {
        if(cursor >= this.src.size()) return new Type.Pchar(Type.EOF, false);
        return this.src.get(cursor);
    }

    public Type.Pchar peekNext() {
        if(cursor + 1 >= this.src.size()) return new Type.Pchar(Type.EOF, false);
        return this.src.get(cursor + 1);
    }

    public Type.Pchar advance() {
        if(cursor >= this.src.size()) return new Type.Pchar(Type.EOF, false);
        Type.Pchar c = this.peek();
        cursor++;
        return c;
    }

    public Type.Pchar prev() {
        if(cursor == 0) throw new YsharpException(
                YsharpException.YsharpErrorType.PROCESS,
                -1,
                "Cannot move to previous character: cursor is already at the beginning of the source.");

        return this.src.get(cursor -1);
    }

    public boolean match(char expected) {
        if(cursor >= this.src.size()) return false;
        if(this.src.get(cursor).c == expected) {
            this.cursor++;
            return  true;
        }
        else {
            return false;
        }
    }

    public Type.Pchar getChar(int position) {
        return this.src.get(position);
    }

    public Type.Pchar getCharOrDefault(int position, Type.Pchar defaultValue) {
        if (position < 0 || position >= this.src.size()) {
            return defaultValue;
        }

        return this.src.get(position);
    }

    public boolean isEnd() {
        return cursor >= this.src.size();
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

    public boolean isAlpha(Type.Pchar p) {
        return p != null && !p.isEscaped && isAlpha(p.c);
    }

    public boolean isNumeric(Type.Pchar p) {
        return p != null && !p.isEscaped && isNumeric(p.c);
    }

    public boolean isAlphaNumeric(Type.Pchar p) {
        return p != null && !p.isEscaped && isAlphaNumeric(p.c);
    }

    public boolean isAlphaOrUnderscore(Type.Pchar p) {
        return p != null && !p.isEscaped && isAlphaOrUnderscore(p.c);
    }

    public boolean isAlphaNumericOrUnderscore(Type.Pchar p) {
        return p != null && !p.isEscaped && isAlphaNumericOrUnderscore(p.c);
    }
}
