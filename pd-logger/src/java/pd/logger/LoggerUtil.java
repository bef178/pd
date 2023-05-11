package pd.logger;

import java.io.IOException;
import java.io.Writer;

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
    public static void writeLine(Writer w, String fieldSeparator, long timestamp, String hostname, LogLevel level,
            String message) throws IOException {
        // TODO csv
        w.write(TimeExtension.toUtcString(timestamp) + fieldSeparator + hostname + fieldSeparator +
                level.toString() + fieldSeparator + message + '\n');
    }
}
