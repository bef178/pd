package pd.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import pd.fenc.CurvePattern;
import pd.time.Ctime;

public class Util {

    public static String evaluateMessage(String message, Object... messageArguments) {
        return CurvePattern.format(message, messageArguments);
    }

    public static String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UnknownHostExceptionHostname";
        } catch (Exception e) {
            return "ExceptionHostname";
        }
    }

    private static InputStream getInputStream(String file) {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        return currentClassLoader != null ? currentClassLoader.getResourceAsStream(file)
                : ClassLoader.getSystemResourceAsStream(file);
    }

    public static Properties loadProperties(String file) {
        try (InputStream stream = getInputStream(file)) {
            if (stream != null) {
                Properties props = new Properties();
                props.load(stream);
                return props;
            }
        } catch (IOException e) {
            // dummy
        }
        return null;
    }

    /**
     * actual logger would call this to write
     */
    public static void writeLine(Writer w, String fieldSeparator, long timestamp, String hostname, LogLevel level,
            String message) throws IOException {
        // TODO csv
        StringBuilder sb = new StringBuilder();
        sb.append(Ctime.toUtcString(timestamp)).append(fieldSeparator).append(hostname).append(fieldSeparator)
                .append(level.toString()).append(fieldSeparator).append(message).append('\n');
        w.write(sb.toString());
    }
}
