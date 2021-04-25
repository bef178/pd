package pd.log;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import pd.time.TimeUtil;

import java.util.PrimitiveIterator.OfInt;

public class LogUtil {

    /**
     * use '{}' as formatting anchor
     */
    public static String evaluateMessage(String message, Object... messageArguments) {
        if (messageArguments == null || messageArguments.length == 0) {
            return message;
        }

        // state machine
        StringBuilder sb = new StringBuilder();
        OfInt it = message.codePoints().iterator();
        int nextArgumentIndex = 0;
        int state = 0;
        while (state != 3) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.nextInt() : -1;
                    switch (ch) {
                        case '\\':
                            state = 1;
                            break;
                        case '{':
                            state = 2;
                            break;
                        case -1:
                            state = 3;
                            break;
                        default:
                            state = 0;
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.nextInt() : -1;
                    switch (ch) {
                        case '\\':
                            state = 0;
                            sb.append('\\');
                            break;
                        case '{':
                            state = 0;
                            sb.append('{');
                            break;
                        default:
                            String actual = new String(Character.toChars(ch));
                            throw new IllegalArgumentException(String.format("E: unrecognized \"\\%s\"", actual));
                    }
                    break;
                }
                case 2: {
                    int ch = it.hasNext() ? it.nextInt() : -1;
                    switch (ch) {
                        case '}':
                            state = 0;
                            if (nextArgumentIndex < messageArguments.length) {
                                sb.append(messageArguments[nextArgumentIndex++]);
                            } else {
                                sb.append("{}");
                            }
                            break;
                        default:
                            state = 0;
                            sb.append('{');
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
            }
        }
        return sb.toString();
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
        sb.append(TimeUtil.toUtcString(timestamp)).append(fieldSeparator).append(hostname).append(fieldSeparator)
                .append(level.toString()).append(fieldSeparator).append(message).append('\n');
        w.write(sb.toString());
    }
}
