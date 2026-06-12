package my.help.useful;

import java.io.*;
import java.nio.file.*;

public class JavaClassesAggregator {
    public static void main(String[] args) throws IOException {
        Path sourceRoot = Paths.get("./src/main/java/my/help/useful/kanban/planning");
        Path outputFile = Paths.get("all_classes.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile)) {
            writer.write("// Java classes from: " + sourceRoot.toAbsolutePath());
            writer.newLine();
            writer.write("// Generated: " + new java.util.Date());
            writer.newLine();
            writer.newLine();

            Files.walk(sourceRoot)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .forEach(file -> {
                        try {
                            writer.write("=== File: " + file.toString() + " ===");
                            writer.newLine();
                            Files.lines(file).forEach(line -> {
                                try {
                                    writer.write(line);
                                    writer.newLine();
                                } catch (IOException e) { throw new UncheckedIOException(e); }
                            });
                            writer.newLine();
                        } catch (IOException e) { throw new UncheckedIOException(e); }
                    });
        }
        System.out.println("Done: " + outputFile.toAbsolutePath());
    }
}