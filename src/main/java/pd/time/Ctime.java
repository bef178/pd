package pd.time;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * transformation between timestamp and local easy-to-read time
 */
public final class Ctime extends Cdate {

    public enum TimeField {

        YEAR, DAY_OF_YEAR, MONTH_OF_YEAR, DAY_OF_MONTH, WEEK_OF_YEAR, DAY_OF_WEEK,
        MILLISECONDS_OF_DAY, HH, MM, SS, SSS;

        private static final TimeField[] values = TimeField.values();

        public static final TimeField fromOrdinal(int ordinal) {
            return values[ordinal];
        }

        static int size() {
            return values.length;
        }
    }

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;
    public static final int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;
    public static final int MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;

    private static final String UTC_TIME_FORMAT = "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ";

    private static final Pattern UTC_TIME_REGEXP = Pattern.compile(
            "^(\\d+)-(\\d{2})-(\\d{2})T(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})Z$");

    protected static void breakMillisecondOfDay(int millisecondOfDay, int[] outFieldValues) {
        assert millisecondOfDay >= 0 && millisecondOfDay <= MILLISECONDS_PER_DAY;
        assert outFieldValues.length == TimeField.size();

        outFieldValues[TimeField.MILLISECONDS_OF_DAY.ordinal()] = millisecondOfDay;

        if (millisecondOfDay == MILLISECONDS_PER_DAY) {
            // troublesome leap second
            outFieldValues[TimeField.HH.ordinal()] = 23;
            outFieldValues[TimeField.MM.ordinal()] = 59;
            outFieldValues[TimeField.SS.ordinal()] = 60;
            outFieldValues[TimeField.SSS.ordinal()] = 0;
            return;
        }

        outFieldValues[TimeField.HH.ordinal()] = (millisecondOfDay / MILLISECONDS_PER_HOUR) % 24;
        outFieldValues[TimeField.MM.ordinal()] = (millisecondOfDay / MILLISECONDS_PER_MINUTE) % 60;
        outFieldValues[TimeField.SS.ordinal()] = (millisecondOfDay / MILLISECONDS_PER_SECOND) % 60;
        outFieldValues[TimeField.SSS.ordinal()] = millisecondOfDay % MILLISECONDS_PER_SECOND;
    }

    public static int[] breakMilliseconds(long millisecondsSinceEpoch) {

        int[] fieldValues = new int[TimeField.size()];

        int millisecondOfDay = (int) (millisecondsSinceEpoch % MILLISECONDS_PER_DAY);
        if (millisecondOfDay < 0) {
            millisecondOfDay += MILLISECONDS_PER_DAY;
        }

        breakMillisecondOfDay(millisecondOfDay, fieldValues);

        long daysSinceEpoch = (millisecondsSinceEpoch - millisecondOfDay) / MILLISECONDS_PER_DAY;
        breakDays(daysSinceEpoch, fieldValues);

        return fieldValues;
    }

    public static long fromUtcString(String utcString) {
        Matcher matcher = UTC_TIME_REGEXP.matcher(utcString);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        int year = Integer.parseInt(matcher.group(1));
        int monthOfYear = Integer.parseInt(matcher.group(2)) - 1;
        int dayOfMonth = Integer.parseInt(matcher.group(3)) - 1;
        int hh = Integer.parseInt(matcher.group(4));
        int mm = Integer.parseInt(matcher.group(5));
        int ss = Integer.parseInt(matcher.group(6));
        int sss = Integer.parseInt(matcher.group(7));

        int dayOfYear = toDayOfYear(year, monthOfYear, dayOfMonth);
        return totalMilliseconds(totalDays(year, dayOfYear), toMillisecondOfDay(hh, mm, ss, sss));
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    public static int toMillisecondOfDay(int hh, int mm, int ss, int sss) {
        assert hh >= 0 && hh < 60;
        assert mm >= 0 && mm < 60;
        assert ss >= 0 && ss < 60;
        assert sss >= 0 && sss < 1000;
        return MILLISECONDS_PER_HOUR * hh + MILLISECONDS_PER_MINUTE * mm
                + MILLISECONDS_PER_SECOND * ss + sss;
    }

    public static long totalMilliseconds(long days, int millisecondOfDay) {
        assert millisecondOfDay >= 0 && millisecondOfDay < MILLISECONDS_PER_DAY;
        return MILLISECONDS_PER_DAY * days + millisecondOfDay;
    }

    public static String toUtcString(int[] fieldValues) {
        return toUtcString(UTC_TIME_FORMAT, fieldValues);
    }

    public static String toUtcString(long millisecondsSinceEpoch) {
        return toUtcString(UTC_TIME_FORMAT, millisecondsSinceEpoch);
    }

    private static String toUtcString(String format, int[] fieldValues) {
        return String.format(format,
                fieldValues[TimeField.YEAR.ordinal()],
                fieldValues[TimeField.MONTH_OF_YEAR.ordinal()] + 1,
                fieldValues[TimeField.DAY_OF_MONTH.ordinal()] + 1,
                fieldValues[TimeField.HH.ordinal()],
                fieldValues[TimeField.MM.ordinal()],
                fieldValues[TimeField.SS.ordinal()],
                fieldValues[TimeField.SSS.ordinal()]);
    }

    public static String toUtcString(String format, long millisecondsSinceEpoch) {
        int[] fieldValues = breakMilliseconds(millisecondsSinceEpoch);
        return toUtcString(format, fieldValues);
    }

    private Ctime() {
        // private dummy
    }
}
