package ysh;

import java.util.ArrayList;
import java.util.List;

public class Preprocess {

    private static List<Type.Pchar> removeEscape(String program) {
        List<Type.Pchar> charStream = new ArrayList<>();
        boolean isEscaped = false;
        for(char c : program.toCharArray()) {
            if(c == '^') {
                if(!isEscaped) {
                    isEscaped = true;
                    continue; // consume ^
                }
                else {
                    charStream.add(new Type.Pchar(c, true));
                }
            }
            else {
                charStream.add(new Type.Pchar(c, isEscaped));
            }

            isEscaped = false;
        }

        return charStream;
    }

    private static List<Type.Pchar> removeComments(List<Type.Pchar> charStream) {
        List<Type.Pchar> processed = new ArrayList<>();
        int cursor = 0;
        while (cursor < charStream.size()) {
            Type.Pchar pChar = charStream.get(cursor);
            if (pChar.c == '#' && !pChar.isEscaped) {
                while (cursor < charStream.size() && charStream.get(cursor).c != '\n') {
                    cursor++;
                }
                continue;
            }
            processed.add(pChar);
            cursor++;
        }
        return processed;
    }

    public static List<Type.Pchar> preprocess(String program) {
        return  removeComments(removeEscape(program));
    }
}
