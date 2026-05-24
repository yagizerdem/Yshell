package ysh;

import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final List<Type.Token> tokens;
    public int cursor;

    public Parser(List<Type.Token> tokens) {
        this.tokens = tokens;
        this.cursor = 0;
    }


}
