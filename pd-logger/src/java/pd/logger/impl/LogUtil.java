package pd.logger.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import lombok.SneakyThrows;
import pd.fenc.CurlyBracketPatternExtension;
import pd.logger.LogLevel;
import pd.time.TimeExtension;

class LogUtil {

    public static String getHostname() {
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null) {
            return hostname;
        }

        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeLine(Writer w, LogEntry logEntry) throws IOException {
        long timestamp = logEntry.timestamp;
        String hostname = logEntry.hostname;
        LogLevel level = logEntry.logLevel;
        String message;
        {
            StringBuilder sb = new StringBuilder();
            sb.append(CurlyBracketPatternExtension.format(logEntry.message, logEntry.messageParams));
            if (logEntry.throwable != null) {
                sb.append(' ').append(throwableToString(logEntry.throwable));
            }
            message = sb.toString();
        }
        writeLine(w, timestamp, hostname, level, message);
    }

    /**
     * actual logger would call this to write
     */
    public static void writeLine(Writer w, long timestamp, String hostname, LogLevel level, String message) throws IOException {
        // TODO csv
        final String fieldSeparator = ",";
        w.write(TimeExtension.toUtcString(timestamp) + fieldSeparator + hostname + fieldSeparator +
                level.toString() + fieldSeparator + message + '\n');
    }

    @SneakyThrows
    public static String throwableToString(Throwable throwable) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(outputStream, true, StandardCharsets.UTF_8.name())) {
            throwable.printStackTrace(ps);
        }
        return outputStream.toString(StandardCharsets.UTF_8.name());
    }
}
