package ysh.builtin;

import ysh.Context;
import ysh.Type;
import ysharp.treewalk.YsharpException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecuteCd {

    public static Type.CommandExecutionResponse execute(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        try {
            if(command.args.size() != 2) {
                System.out.println("cd args should max size 2");
                return response;
            }

            String dest = command.args.get(1);
            Context context = Context.getContext();

            if(dest.equals(".")) {
                return response;
            }

            if(dest.equals("..")) {
                Path parent = context.cwd.getParent();

                if (parent == null) {
                    return response;
                }

                if (!Files.exists(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "cd: no such file or directory: " + dest
                    );
                }

                if (!Files.isDirectory(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "cd: not a directory: " + dest
                    );
                }

                context.cwd = parent;
                System.setProperty("user.dir", context.cwd.toString());
                return response;
            }

            Path currentPath = context.cwd;

            Path newPath = Paths.get(dest);
            if (!newPath.isAbsolute()) {
                newPath = currentPath.resolve(dest);
            }

            newPath = newPath.normalize();

            if (!Files.exists(newPath)) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "cd: no such file or directory: " + dest
                );
            }

            if (!Files.isDirectory(newPath)) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "cd: not a directory: " + dest
                );
            }

            context.cwd = newPath;
            System.setProperty("user.dir", newPath.toString());

            Context.getContext().setExitStatus(0);
        }catch (Exception ex) {
            Context.getContext().setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return response;
    }

}
