package my.help.useful.util;

import java.io.IOException;
import java.net.URI;

public class PgDumpWrapper {
    public static void dumpDatabase(String executable, String jdbcUrl, String user, String password, String outputFile)
            throws IOException, InterruptedException {
        URI database = URI.create(jdbcUrl.substring("jdbc:".length()));
        ProcessBuilder builder = new ProcessBuilder(executable, "--column-inserts",
                "-d", database.getPath().substring(1), "-U", user,
                "-h", database.getHost(), "-p", String.valueOf(database.getPort() < 0 ? 5432 : database.getPort()),
                "-f", outputFile);
        builder.environment().put("PGPASSWORD", password);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output = new String(process.getInputStream().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        if (process.waitFor() != 0) {
            throw new IOException("pg_dump failed: " + output);
        }
    }
}
