package pd.time;

import pd.time.Ctime.TimeField;

class Cdate {

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

    protected static void breakDays(final long daysSinceEpoch, int[] outFieldValues) {
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

        outFieldValues[TimeField.YEAR.ordinal()] = year;
        outFieldValues[TimeField.DAY_OF_YEAR.ordinal()] = dayOfYear;
        outFieldValues[TimeField.MONTH_OF_YEAR.ordinal()] = monthOfYear;
        outFieldValues[TimeField.DAY_OF_MONTH.ordinal()] = dayOfMonth;
        outFieldValues[TimeField.WEEK_OF_YEAR.ordinal()] = weekOfYear;
        outFieldValues[TimeField.DAY_OF_WEEK.ordinal()] = dayOfWeek;
    }

    /**
     * Sun. => 0, Mon. => 1, ..., Sat. => 6
     */
    public static int daysToDayOfWeek(long days) {
        int from = 4; // 1970-01-01 is Thursday
        from = (int) ((from + days) % 7);
        if (from < 0) {
            from += 7;
        }
        return from;
    }

    public static int getNumDaysByYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    private static int[] getNumDaysPerEachMonthByYear(int year) {
        return isLeapYear(year) ? MONTH_DAYS_366 : MONTH_DAYS_365;
    }

    private static boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            return year % 400 == 0;
        }
        return year % 4 == 0;
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
}
