package pd.time;

import static pd.time.TimeExtension.INDEX_DAY_OF_MONTH;
import static pd.time.TimeExtension.INDEX_DAY_OF_WEEK;
import static pd.time.TimeExtension.INDEX_DAY_OF_YEAR;
import static pd.time.TimeExtension.INDEX_MONTH_OF_YEAR;
import static pd.time.TimeExtension.INDEX_WEEK_OF_YEAR;
import static pd.time.TimeExtension.INDEX_YEAR_OF_TIME;
import static pd.time.TimeExtension.safeOutput;

class DateExtension {

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

    static void findDate(long daysSinceEpoch, int[] outComponents) {
        // year must in the range of int32
        if (daysSinceEpoch < DAYS_MIN || daysSinceEpoch >= DAYS_MAX) {
            throw new IllegalArgumentException();
        }

        long i = daysSinceEpoch - (Y1 * 31 + 8);

        int n400 = (int) (i / Y400);
        i -= (long) Y400 * n400;
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

        int year = 2001 + 400 * n400 + 100 * n100 + 4 * n4 + n1;
        if (j < 0) {
            year--;
            j += findNumDaysInThatYear(year);
        }

        final int dayOfYear = j;

        final int weekOfYear = (findDayOfWeek(daysSinceEpoch - dayOfYear) + dayOfYear) / 7;
        final int dayOfWeek = findDayOfWeek(daysSinceEpoch);

        int[] monthDays = findMonthDaysInThatYear(year);

        int m = 0;
        while (j >= monthDays[m]) {
            j -= monthDays[m];
            m++;
        }

        final int monthOfYear = m;
        final int dayOfMonth = j;

        safeOutput(outComponents, INDEX_YEAR_OF_TIME, year);
        safeOutput(outComponents, INDEX_DAY_OF_YEAR, dayOfYear);

        safeOutput(outComponents, INDEX_MONTH_OF_YEAR, monthOfYear);
        safeOutput(outComponents, INDEX_DAY_OF_MONTH, dayOfMonth);

        safeOutput(outComponents, INDEX_WEEK_OF_YEAR, weekOfYear);
        safeOutput(outComponents, INDEX_DAY_OF_WEEK, dayOfWeek);
    }

    private static int findNumDaysInThatYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    private static boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            return year % 400 == 0;
        }
        return year % 4 == 0;
    }

    static int findDayOfWeek(long daysSinceEpoch) {
        int from = 4; // 1970-01-01 is Thursday
        from = (int) ((from + daysSinceEpoch) % 7);
        if (from < 0) {
            from += 7;
        }
        return from;
    }

    private static int[] findMonthDaysInThatYear(int year) {
        return isLeapYear(year) ? MONTH_DAYS_366 : MONTH_DAYS_365;
    }

    static long toDaysSinceEpoch(int year, int monthOfYear, int dayOfMonth) {
        return toDaysSinceEpoch(year, toDayOfYear(year, monthOfYear, dayOfMonth));
    }

    static long toDaysSinceEpoch(int year, int dayOfYear) {
        if (dayOfYear < 0 || dayOfYear > findNumDaysInThatYear(year)) {
            throw new IllegalArgumentException();
        }

        // align to year 0 to avoid overflow
        int numLeapYears = year > 0
                ? (1 + (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400)
                : (year / 4 - year / 100 + year / 400);
        return -719528 + 365L * year + numLeapYears + dayOfYear;
    }

    static int toDayOfYear(int year, int monthOfYear, int dayOfMonth) {
        if (monthOfYear < 0 || monthOfYear >= 12) {
            throw new IllegalArgumentException();
        }

        int[] monthDays = findMonthDaysInThatYear(year);
        if (dayOfMonth < 0 || dayOfMonth >= monthDays[monthOfYear]) {
            throw new IllegalArgumentException();
        }

        int dayOfYear = dayOfMonth;
        for (int i = 0; i < monthOfYear; i++) {
            dayOfYear += monthDays[i];
        }
        return dayOfYear;
    }
}
