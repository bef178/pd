package pd.time.calendar.gregorian;

final class TimeUtil {

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;
    public static final int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;
    public static final int MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;

    public static long extractDays(long milliseconds) {
        int millisecondOfDay = extractMillisecondOfDay(milliseconds);
        return (milliseconds - millisecondOfDay) / MILLISECONDS_PER_DAY;
    }

    public static long extractDays(long milliseconds, int millisecondOfDay) {
        return (milliseconds - millisecondOfDay) / MILLISECONDS_PER_DAY;
    }

    /**
     * must be positive or 0
     */
    public static int extractMillisecondOfDay(long milliseconds) {
        milliseconds = milliseconds % MILLISECONDS_PER_DAY;
        if (milliseconds < 0) {
            milliseconds += MILLISECONDS_PER_DAY;
        }
        return (int) milliseconds;
    }

    public static long sumMilliseconds(long days, int millisecondOfDay) {
        assert millisecondOfDay >= 0 && millisecondOfDay < MILLISECONDS_PER_DAY;
        return MILLISECONDS_PER_DAY * days + millisecondOfDay;
    }

    private TimeUtil() {
        // private dummy
    }
}
