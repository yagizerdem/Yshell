package ysh;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Globber {

    public static List<String> expandGlob(String globPattern) {
        Context context = Context.getContext();

        Path baseDir = context.cwd.toAbsolutePath().normalize();

        String normalizedPattern = globPattern.replace("\\", "/");

        PathMatcher matcher = FileSystems.getDefault()
                .getPathMatcher("glob:" + normalizedPattern);

        List<String> matches = new ArrayList<>();

        try {
            Files.walk(baseDir)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        Path relative = baseDir.relativize(path);
                        String relativeText = relative.toString().replace("\\", "/");

                        if (matcher.matches(Paths.get(relativeText))) {
                            matches.add(relativeText);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException("Glob expansion failed: " + globPattern, e);
        }

        Collections.sort(matches);

        return matches.isEmpty()
                ? List.of()
                : matches;
    }
}
