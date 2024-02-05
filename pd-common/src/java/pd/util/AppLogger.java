package pd.util;

public class AppLogger {

    public static void stdout(String message, Object... messageParams) {
        System.out.println(CurlyBracketPatternExtension.format(message, messageParams));
    }

    public static void stdoutNoNewLine(String message, Object... messageParams) {
        System.out.print(CurlyBracketPatternExtension.format(message, messageParams));
    }

    public static void stderr(String message, Object... messageParams) {
        System.err.println(CurlyBracketPatternExtension.format(message, messageParams));
    }

    public static final AppLogger one = new AppLogger();

    public static AppLogger singleton() {
        return one;
    }

    public void error(String message, Object... messageParams) {
        stderr(message, messageParams);
    }

    public void info(String message, Object... messageParams) {
        stdout(message, messageParams);
    }
}
