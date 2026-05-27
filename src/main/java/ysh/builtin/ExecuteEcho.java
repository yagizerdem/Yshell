package ysh.builtin;

import ysh.Context;
import ysh.Type;

public class ExecuteEcho {

    public static Type.CommandExecutionResponse execute(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();

        try {
            if (command.args.size() == 1) {
                System.out.println();
                Context.getContext().setExitStatus(0);
                return response;
            }

            StringBuilder builder = new StringBuilder();

            for (int i = 1; i < command.args.size(); i++) {
                if (i > 1) {
                    builder.append(" ");
                }

                builder.append(command.args.get(i));
            }

            if(options.captureStdout) {
                response.stdOut += builder.toString();
            }
            else {
                System.out.println(builder.toString());
            }
            Context.getContext().setExitStatus(0);

        } catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.err.println(ex.getMessage());
        }
        return response;
    }

}
