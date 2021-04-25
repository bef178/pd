package pd.time;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * transformation between timestamp and local easy-to-read time
 */
public final class TimeUtil {

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

    /**
     * included
     */
    public static final long DAYS_MIN = -784353015833L;

    /**
     * excluded
     */
    public static final long DAYS_MAX = 784351576777L;

    private static final int Y1 = 365;
    private static final int Y4 = Y1 * 4 + 1; // 1461
    private static final int Y100 = Y4 * 25 - 1; // 36524
    private static final int Y400 = Y100 * 4 + 1; // 146097

    private static final int[] MONTH_DAYS_365 = {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private static final int[] MONTH_DAYS_366 = {
            31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private static boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            return year % 400 == 0;
        }
        return year % 4 == 0;
    }

    public static int getNumDaysByYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    private static int[] getNumDaysPerEachMonthByYear(int year) {
        return isLeapYear(year) ? MONTH_DAYS_366 : MONTH_DAYS_365;
    }

    public static int daysToDayOfWeek(long days) {
        int from = 4; // 1970-01-01 is Thursday
        from = (int) ((from + days) % 7);
        if (from < 0) {
            from += 7;
        }
        return from;
    }

    public static int[] getTimeFieldValues(long millisecondsSinceEpoch) {

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

    private static void breakDays(final long daysSinceEpoch, int[] fieldValues) {
        // ensure year in range of int32
        assert daysSinceEpoch >= -784353015833L && daysSinceEpoch < 784351576777L;

        long i = daysSinceEpoch - (Y1 * 30 + 7);

        int n400 = (int) (i / Y400);
        i -= Y400 * n400;
        if (i < 0) {
            n400--;
            i += Y400;
        }
        assert i >= 0 && i < Y400;

        int j = (int) i;

        int n100 = j / Y100;
        if (n100 == 4) {
            n100--;
        }
        j -= Y100 * n100;

        int n4 = j / Y4;
        j -= Y4 * n4;

        int n1 = j / Y1;
        if (n1 == 4) {
            n1--;
        }
        j -= Y1 * n1;
        if (n1 > 0) {
            j--;
        }

        int year = 2000 + 400 * n400 + 100 * n100 + 4 * n4 + 1 * n1;
        if (j < 0) {
            year--;
            j += getNumDaysByYear(year);
        }

        final int dayOfYear = j;

        final int weekOfYear = (daysToDayOfWeek(daysSinceEpoch - dayOfYear) + dayOfYear) / 7;
        final int dayOfWeek = daysToDayOfWeek(daysSinceEpoch);

        int[] monthDays = getNumDaysPerEachMonthByYear(year);

        int m = 0;
        while (j >= monthDays[m]) {
            j -= monthDays[m];
            m++;
        }

        final int monthOfYear = m;
        final int dayOfMonth = j;

        fieldValues[TimeField.YEAR.ordinal()] = year;
        fieldValues[TimeField.DAY_OF_YEAR.ordinal()] = dayOfYear;
        fieldValues[TimeField.MONTH_OF_YEAR.ordinal()] = monthOfYear;
        fieldValues[TimeField.DAY_OF_MONTH.ordinal()] = dayOfMonth;
        fieldValues[TimeField.WEEK_OF_YEAR.ordinal()] = weekOfYear;
        fieldValues[TimeField.DAY_OF_WEEK.ordinal()] = dayOfWeek;
    }

    public static int toDayOfYear(final int year, final int monthOfYear, final int dayOfMonth) {
        assert monthOfYear >= 0 && monthOfYear < 12;
        assert dayOfMonth >= 0 && dayOfMonth < getNumDaysPerEachMonthByYear(year)[monthOfYear];

        int[] monthDays = getNumDaysPerEachMonthByYear(year);
        int dayOfYear = dayOfMonth;
        for (int i = 0; i < monthOfYear; i++) {
            dayOfYear += monthDays[i];
        }
        return dayOfYear;
    }

    public static long totalDays(final int year, final int dayOfYear) {
        assert dayOfYear >= 0 && dayOfYear < getNumDaysByYear(year);
        // align to year 0 to avoid overflow
        int numLeapYears = year > 0
                ? (1 + (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400)
                : (year / 4 - year / 100 + year / 400);
        return -719528 + 1L * 365 * year + numLeapYears + dayOfYear;
    }

    private static void breakMillisecondOfDay(int millisecondOfDay, int[] fieldValues) {
        assert millisecondOfDay >= 0 && millisecondOfDay <= MILLISECONDS_PER_DAY;
        assert fieldValues.length == TimeField.size();

        fieldValues[TimeField.MILLISECONDS_OF_DAY.ordinal()] = millisecondOfDay;

        if (millisecondOfDay == MILLISECONDS_PER_DAY) {
            // troublesome leap second
            fieldValues[TimeField.HH.ordinal()] = 23;
            fieldValues[TimeField.MM.ordinal()] = 59;
            fieldValues[TimeField.SS.ordinal()] = 60;
            fieldValues[TimeField.SSS.ordinal()] = 0;
            return;
        }

        fieldValues[TimeField.HH.ordinal()] = (millisecondOfDay / MILLISECONDS_PER_HOUR) % 24;
        fieldValues[TimeField.MM.ordinal()] = (millisecondOfDay / MILLISECONDS_PER_MINUTE) % 60;
        fieldValues[TimeField.SS.ordinal()] = (millisecondOfDay / MILLISECONDS_PER_SECOND) % 60;
        fieldValues[TimeField.SSS.ordinal()] = millisecondOfDay % MILLISECONDS_PER_SECOND;
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

    public static String toUtcString(long millisecondsSinceEpoch) {
        return toUtcString(UTC_TIME_FORMAT, millisecondsSinceEpoch);
    }

    public static String toUtcString(String format, long millisecondsSinceEpoch) {
        int[] fieldValues = getTimeFieldValues(millisecondsSinceEpoch);
        return String.format(format,
                fieldValues[TimeField.YEAR.ordinal()],
                fieldValues[TimeField.MONTH_OF_YEAR.ordinal()] + 1,
                fieldValues[TimeField.DAY_OF_MONTH.ordinal()] + 1,
                fieldValues[TimeField.HH.ordinal()],
                fieldValues[TimeField.MM.ordinal()],
                fieldValues[TimeField.SS.ordinal()],
                fieldValues[TimeField.SSS.ordinal()]);
    }

    public static long now() {
        return System.currentTimeMillis();
    }

    private TimeUtil() {
        // private dummy
    }
}
