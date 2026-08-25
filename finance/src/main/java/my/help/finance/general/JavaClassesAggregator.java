package my.help.finance.general;

import java.io.*;
import java.nio.file.*;

public class JavaClassesAggregator {
    public static void main(String[] args) throws IOException {
        Path sourceRoot = Paths.get("./finance/src/main/java/my/help/finance/general");
        Path outputFile = Paths.get("all_classes.txt");

        try (BufferedWriter writer = Files.newBufferedWriter(outputFile);
             var stream = Files.walk(sourceRoot)) {          // ← закрываем стрим

            writer.write("// Java classes from: " + sourceRoot.toAbsolutePath());
            writer.newLine();
            writer.write("// Generated: " + new java.util.Date());
            writer.newLine();
            writer.newLine();

            stream.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .sorted()
                    .forEach(file -> {
                        try {
                            writer.write("=== File: " + file + " ===");
                            writer.newLine();
                            Files.lines(file).forEach(line -> {
                                try {
                                    writer.write(line);
                                    writer.newLine();
                                } catch (IOException e) {
                                    throw new UncheckedIOException(e);
                                }
                            });
                            writer.newLine();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
        }
        System.out.println("Done: " + outputFile.toAbsolutePath());
    }
}