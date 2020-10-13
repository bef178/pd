package pd.time;

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

    private static final int[] MONTH_DAYS = {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private static final int[] MONTH_DAYS_366 = {
            31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    static int[] daysPerMonth(int year) {
        return isLeapYear(year) ? MONTH_DAYS_366 : MONTH_DAYS;
    }

    static int daysPerYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    static int daysToDayOfWeek(long days) {
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
        fieldValues[TimeField.MILLISECONDS_OF_DAY.ordinal()] = millisecondOfDay;
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
            j += daysPerYear(year);
        }

        final int dayOfYear = j;

        final int weekOfYear = (daysToDayOfWeek(daysSinceEpoch - dayOfYear) + dayOfYear) / 7;
        final int dayOfWeek = daysToDayOfWeek(daysSinceEpoch);

        int[] monthDays = daysPerMonth(year);

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

    private static boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            return year % 400 == 0;
        }
        return year % 4 == 0;
    }

    private static void breakMillisecondOfDay(int millisecondOfDay, int[] fieldValues) {
        assert millisecondOfDay >= 0 && millisecondOfDay <= MILLISECONDS_PER_DAY;
        assert fieldValues.length == TimeField.size();

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

    static long millisecondsToDays(long milliseconds) {
        int millisecondOfDay = millisecondsToMillisecondOfDay(milliseconds);
        return (milliseconds - millisecondOfDay) / MILLISECONDS_PER_DAY;
    }

    static long millisecondsToDays(long milliseconds, int millisecondOfDay) {
        return (milliseconds - millisecondOfDay) / MILLISECONDS_PER_DAY;
    }

    /**
     * must be positive or 0
     */
    static int millisecondsToMillisecondOfDay(long milliseconds) {
        milliseconds = milliseconds % MILLISECONDS_PER_DAY;
        if (milliseconds < 0) {
            milliseconds += MILLISECONDS_PER_DAY;
        }
        return (int) milliseconds;
    }

    static int toDayOfYear(final int year, final int monthOfYear, final int dayOfMonth) {
        assert monthOfYear >= 0 && monthOfYear < 12;
        assert dayOfMonth >= 0 && dayOfMonth < daysPerMonth(year)[monthOfYear];

        int[] monthDays = daysPerMonth(year);
        int dayOfYear = dayOfMonth;
        for (int i = 0; i < monthOfYear; i++) {
            dayOfYear += monthDays[i];
        }
        return dayOfYear;
    }

    static long toDays(final int year, final int dayOfYear) {
        assert dayOfYear >= 0 && dayOfYear < daysPerYear(year);
        // align to year 0 to avoid overflow
        int numLeapYears = year > 0
                ? (1 + (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400)
                : (year / 4 - year / 100 + year / 400);
        return -719528 + 1L * 365 * year + numLeapYears + dayOfYear;
    }

    static int toMillisecondOfDay(int hh, int mm, int ss, int sss) {
        assert hh >= 0 && hh < 60;
        assert mm >= 0 && mm < 60;
        assert ss >= 0 && ss < 60;
        assert sss >= 0 && sss < 1000;
        return MILLISECONDS_PER_HOUR * hh + MILLISECONDS_PER_MINUTE * mm
                + MILLISECONDS_PER_SECOND * ss + sss;
    }

    static long toMilliseconds(long days, int millisecondOfDay) {
        assert millisecondOfDay >= 0 && millisecondOfDay < MILLISECONDS_PER_DAY;
        return MILLISECONDS_PER_DAY * days + millisecondOfDay;
    }

    private TimeUtil() {
        // private dummy
    }
}
