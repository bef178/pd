package pd.time;

/**
 * timestamp in milliseconds <==> easy-to-read time
 */
public class TimeExtension {

    public static final int INDEX_YEAR_OF_TIME = 0;

    /**
     * [0,11], Jan = 0
     */
    public static final int INDEX_MONTH_OF_YEAR = 1;

    /**
     * [0,30]
     */
    public static final int INDEX_DAY_OF_MONTH = 2;

    /**
     * [0,23]
     */
    public static final int INDEX_HOUR_OF_DAY = 3;

    /**
     * [0,59]
     */
    public static final int INDEX_MINUTE_OF_HOUR = 4;

    /**
     * [0,59]
     */
    public static final int INDEX_SECOND_OF_MINUTE = 5;

    /**
     * [0,999]
     */
    public static final int INDEX_MILLISECOND_OF_SECOND = 6;

    /**
     * [0,365]
     */
    public static final int INDEX_DAY_OF_YEAR = 7;

    /**
     * [0,52], week 0 covers the first day through the first Saturday
     */
    public static final int INDEX_WEEK_OF_YEAR = 8;

    /**
     * [0,6], Sunday = 0, Monday = 1
     */
    public static final int INDEX_DAY_OF_WEEK = 9;

    /**
     * [0, 86400000)
     */
    public static final int INDEX_MILLISECOND_OF_DAY = 10;

    private static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    private static final int MILLISECONDS_PER_HOUR = 60 * 60 * 1000;
    static final int MILLISECONDS_PER_MINUTE = 60 * 1000;
    static final int MILLISECONDS_PER_SECOND = 1000;

    public static final String UTC_TIME_FORMAT = "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ";

    public static int[] createTimeComponents() {
        return new int[11];
    }

    public static int[] findTimeComponents(long millisecondsSinceEpoch) {
        return findTimeComponents(millisecondsSinceEpoch, createTimeComponents());
    }

    public static int[] findTimeComponents(long millisecondsSinceEpoch, int[] outComponents) {
        // unnecessary to check data range

        long daysSinceEpoch = millisecondsSinceEpoch / MILLISECONDS_PER_DAY;
        int millisecondOfDay = (int) (millisecondsSinceEpoch - daysSinceEpoch * MILLISECONDS_PER_DAY);
        if (millisecondOfDay < 0) {
            daysSinceEpoch--;
            millisecondOfDay += MILLISECONDS_PER_DAY;
        }

        safeOutput(outComponents, INDEX_MILLISECOND_OF_DAY, millisecondOfDay);

        findDatePart(daysSinceEpoch, outComponents);
        findTimePart(millisecondOfDay, outComponents);

        return outComponents;
    }

    public static void findDatePart(long daysSinceEpoch, int[] outComponents) {
        DateExtension.findDatePart(daysSinceEpoch, outComponents);
    }

    public static void findTimePart(int millisecondOfDay, int[] outComponents) {
        if (millisecondOfDay < 0 || millisecondOfDay >= MILLISECONDS_PER_DAY) {
            throw new IllegalArgumentException();
        }

        int milliseconds = millisecondOfDay;

        final int hourOfDay = milliseconds / MILLISECONDS_PER_HOUR;
        milliseconds -= hourOfDay * MILLISECONDS_PER_HOUR;
        safeOutput(outComponents, INDEX_HOUR_OF_DAY, hourOfDay);

        final int minuteOfHour = milliseconds / MILLISECONDS_PER_MINUTE;
        milliseconds -= minuteOfHour * MILLISECONDS_PER_MINUTE;
        safeOutput(outComponents, INDEX_MINUTE_OF_HOUR, minuteOfHour);

        final int secondOfMinute = milliseconds / MILLISECONDS_PER_SECOND;
        milliseconds -= secondOfMinute * MILLISECONDS_PER_SECOND;
        safeOutput(outComponents, INDEX_SECOND_OF_MINUTE, secondOfMinute);

        safeOutput(outComponents, INDEX_MILLISECOND_OF_SECOND, milliseconds);
    }

    static boolean safeOutput(int[] outComponents, int index, int value) {
        if (outComponents != null && index >= 0 && index < outComponents.length) {
            outComponents[index] = value;
            return true;
        }
        return false;
    }

    public static long toMillisecondsSinceEpoch(long daysSinceEpoch, int millisecondOfDay) {
        if (millisecondOfDay < 0 || millisecondOfDay >= MILLISECONDS_PER_DAY) {
            throw new IllegalArgumentException();
        }
        return MILLISECONDS_PER_DAY * daysSinceEpoch + millisecondOfDay;
    }

    static int toMillisecondOfDay(int hour, int minute, int second, int millisecond) {
        if (hour < 0 || hour >= 24
                || minute < 0 || minute >= 60
                || second < 0 || second >= 60
                || millisecond < 0 || millisecond >= 1000) {
            throw new IllegalArgumentException();
        }
        return hour * MILLISECONDS_PER_HOUR
                + minute * MILLISECONDS_PER_MINUTE
                + second * MILLISECONDS_PER_SECOND
                + millisecond;
    }

    public static String toUtcString(long millisecondsSinceEpoch) {
        return toUtcString(millisecondsSinceEpoch, UTC_TIME_FORMAT);
    }

    public static String toUtcString(long millisecondsSinceEpoch, String format) {
        int[] components = findTimeComponents(millisecondsSinceEpoch);
        return toUtcString(components, format);
    }

    public static String toUtcString(int[] components, String format) {
        return String.format(format,
                components[INDEX_YEAR_OF_TIME],
                components[INDEX_MONTH_OF_YEAR] + 1,
                components[INDEX_DAY_OF_MONTH] + 1,
                components[INDEX_HOUR_OF_DAY],
                components[INDEX_MINUTE_OF_HOUR],
                components[INDEX_SECOND_OF_MINUTE],
                components[INDEX_MILLISECOND_OF_SECOND]);
    }

    private TimeExtension() {
        // dummy
    }
}
