package ysh.builtin;

import ysh.Context;
import ysh.Type;

public class ExecuteExit {
    public static Type.CommandExecutionResponse execute(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        try {
            if(command.args.size() > 2) {
                System.out.println("exit args should max size 2");
                return response;
            }

            int exitStatus = 0;
            if(command.args.size() == 2) {
                String status = command.args.get(1);
                try {
                    exitStatus = Integer.parseInt(status);
                } catch (NumberFormatException ex) {
                    Context.getContext().setExitStatus(1);
                    System.out.println("exit: numeric argument required: " + status);
                    return response;
                }
            }
            System.exit(exitStatus);
        }
        catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.err.println(ex.getMessage());
        }
        return response;
    }
}
