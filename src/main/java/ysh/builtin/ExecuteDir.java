package ysh.builtin;

import ysh.Context;
import ysh.Type;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecuteDir {
    public static Type.CommandExecutionResponse execute(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        try {
            Context context = Context.getContext();

            if(command.args.size() == 1) {
                File file = new File(context.cwd.toString());
                String[] entities = file.list();
                for(String entity : entities) {
                    System.out.println(entity);
                }
                return response;
            }

            for(int i = 1; i < command.args.size(); i++) {
                String part = command.args.get(i);
                Path newPath = Paths.get(part);

                if(!newPath.isAbsolute()) {
                    newPath = context.cwd.resolve(newPath);
                }

                newPath = newPath.normalize();

                if(Files.isDirectory(newPath)) {
                    File file = new File(newPath.toString());
                    String[] entities = file.list();
                    for(String entity : entities) {
                        System.out.println(entity);
                    }
                }
                else {
                    System.out.println(newPath);
                }
            }
        }
        catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return response;
    }
}
