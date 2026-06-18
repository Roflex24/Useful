package my.help.useful.util;

import java.io.*;

public class PgDumpWrapper {
    public static void dumpDatabase(String dbName, String user, String password, String outputFile)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "C:\\Program Files\\PostgreSQL\\18\\bin\\pg_dump.exe",
                "--column-inserts",
                "-d", dbName,
                "-U", user,
                "-h", "localhost",
                "-f", outputFile
        );

        pb.environment().put("PGPASSWORD", password);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode == 0) {
            System.out.println("Dump created successfully: " + outputFile);
        } else {
            // Читаем ошибки
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            }
        }
    }
}