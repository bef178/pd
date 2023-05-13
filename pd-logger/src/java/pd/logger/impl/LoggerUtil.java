package pd.logger.impl;

import java.io.IOException;
import java.io.Writer;

import pd.logger.LogLevel;
import pd.time.TimeExtension;

public class LoggerUtil {

    public static String getHostname() {
        return System.getenv("HOSTNAME");
//        try {
//            return InetAddress.getLocalHost().getHostName();
//        } catch (UnknownHostException e) {
//            return "UnknownHostExceptionHostname";
//        } catch (Exception e) {
//            return "ExceptionHostname";
//        }
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
}
