package ysh.builtin;

import ysh.Context;
import ysh.Cursor;
import ysh.Preprocess;
import ysh.Type;
import ysharp.treewalk.YsharpException;

public class ExecuteSet {

    public static Type.CommandExecutionResponse execute(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        Context context = Context.getContext();
        try {
            if (command.args.size() != 2) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: expected exactly one assignment in the form name=value"
                );
            }

            String assignment = command.args.get(1);
            Cursor cur = new Cursor(Preprocess.preprocess(assignment));

            StringBuilder identifierPart = new StringBuilder();
            StringBuilder valuePart = new StringBuilder();

            if (cur.isEnd()) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: assignment cannot be empty"
                );
            }

            if (cur.peek().c == '=') {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: missing variable name before '='"
                );
            }

            Type.Pchar first = cur.peek();

            if (!cur.isAlphaOrUnderscore(first)) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: invalid variable name: variable name must start with a letter or '_'"
                );
            }

            while (!cur.isEnd()) {
                Type.Pchar pChar = cur.peek();

                if (pChar.c == '=') {
                    cur.advance();
                    break;
                }

                if (!cur.isAlphaNumericOrUnderscore(pChar)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "set: invalid variable name: character '" + pChar.c + "' is not allowed"
                    );
                }

                identifierPart.append(cur.advance());
            }

            if (identifierPart.length() == 0) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: variable name cannot be empty"
                );
            }

            if (cur.prev().c != '=') {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "set: missing '=' in assignment"
                );
            }

            while (!cur.isEnd()) {
                valuePart.append(cur.advance());
            }

            context.env.setVariable(
                    identifierPart.toString(),
                    valuePart.toString()
            );

            context.setExitStatus(0);

        } catch (Exception ex) {
            context.setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return response;
    }

}
