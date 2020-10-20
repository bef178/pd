package pd.log;

import java.net.InetAddress;
import java.net.UnknownHostException;

import pd.time.TimeUtil;

public interface ILogger {

    public void flush();

    public default String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "UnknownHostExceptionHostname";
        } catch (Exception e) {
            return "ExceptionHostname";
        }
    }

    public default void log(LogLevel level, String message, Object... messageArguments) {
        log(TimeUtil.now(), level, message, messageArguments);
    }

    public void log(long timestamp, LogLevel level, String message, Object... messageArguments);

    public default void logError(String message, Object... messageArguments) {
        log(LogLevel.ERROR, message, messageArguments);
    }

    public default void logInfo(String message, Object... messageArguments) {
        log(LogLevel.INFO, message, messageArguments);
    }
}
