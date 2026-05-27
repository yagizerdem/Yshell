package ysh.builtin;

import ysh.Context;
import ysh.Type;
import ysharp.treewalk.YsharpException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExecuteMkdir {
    public static Type.CommandExecutionResponse execute(Type.Command command, Type.CommandExecutionOptions options) {
        Type.CommandExecutionResponse response = new Type.CommandExecutionResponse();
        Context context = Context.getContext();

        try {
            if (command.args.size() < 2) {
                throw new YsharpException(
                        YsharpException.YsharpErrorType.PROCESS,
                        -1,
                        "mkdir: missing operand"
                );
            }

            for (int i = 1; i < command.args.size(); i++) {
                String part = command.args.get(i);

                if (part == null || part.isBlank()) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: invalid empty directory name"
                    );
                }

                Path newPath = Paths.get(part);

                if (!newPath.isAbsolute()) {
                    newPath = context.cwd.resolve(newPath);
                }

                newPath = newPath.normalize();

                if (Files.exists(newPath)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: cannot create directory '" + part + "': file exists"
                    );
                }

                Path parent = newPath.getParent();

                if (parent != null && !Files.exists(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: cannot create directory '" + part + "': parent directory does not exist"
                    );
                }

                if (parent != null && !Files.isDirectory(parent)) {
                    throw new YsharpException(
                            YsharpException.YsharpErrorType.PROCESS,
                            -1,
                            "mkdir: cannot create directory '" + part + "': parent is not a directory"
                    );
                }

                Files.createDirectory(newPath);
            }

            context.setExitStatus(0);

        } catch (Exception ex) {
            context.setExitStatus(1);
            System.out.println(ex.getMessage());
        }
        return  response;
    }
}
