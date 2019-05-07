package pd.time.calendar.gregorian;

import static pd.time.calendar.gregorian.Util.getDaysOfYear;
import static pd.time.calendar.gregorian.Util.getMonthDays;

public enum DateField {

    YEAR, DAY_OF_YEAR, MONTH_OF_YEAR, DAY_OF_MONTH, WEEK_OF_YEAR, DAY_OF_WEEK;

    private static final int Y1 = 365;

    private static final int Y4 = Y1 * 4 + 1; // 1461

    private static final int Y100 = Y4 * 25 - 1; // 36524

    private static final int Y400 = Y100 * 4 + 1; // 146097

    /**
     * return 0-based value
     */
    static int get(final long days, final DateField field) {
        assert days >= -784353015833L && days < 784351576777L; // year in range of int32
        assert field != null;

        if (field == DAY_OF_WEEK) {
            return getDayOfWeek(days);
        }

        long i = days - (Y1 * 30 + 7);

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

        if (field == YEAR) {
            return year;
        }

        int dayOfYear = j;

        if (field == DAY_OF_YEAR) {
            return dayOfYear;
        }

        if (field == WEEK_OF_YEAR) {
            return (getDayOfWeek(days - dayOfYear) + dayOfYear) / 7;
        }

        int[] monthDays = getMonthDays(year);

        int monthOfYear = 0;
        while (j >= monthDays[monthOfYear]) {
            j -= monthDays[monthOfYear];
            monthOfYear++;
        }

        if (field == MONTH_OF_YEAR) {
            return monthOfYear;
        }

        int dayOfMonth = j;

        if (field == DAY_OF_MONTH) {
            return dayOfMonth;
        }

        return -1;
    }

    static int getDayOfWeek(long days) {
        int from = 4; // 1970-01-01 is Thursday
        from = (int) ((from + days) % 7);
        if (from < 0) {
            from += 7;
        }
        return from;
    }

    static long toDays(final int year, final int dayOfYear) {
        assert dayOfYear >= 0 && dayOfYear < getDaysOfYear(year);
        // align to year 0 to avoid overflow
        int numLeapYears = year > 0
                ? (1 + (year - 1) / 4 - (year - 1) / 100 + (year - 1) / 400)
                : (year / 4 - year / 100 + year / 400);
        return -719528 + 1L * 365 * year + numLeapYears + dayOfYear;
    }

    static long toDays(final int year, final int monthOfYear, final int dayOfMonth) {
        return toDays(year, toDayOfYear(year, monthOfYear, dayOfMonth));
    }

    static int toDayOfYear(int year, int monthOfYear, int dayOfMonth) {
        assert monthOfYear >= 0 && monthOfYear < 12;
        assert dayOfMonth >= 0 && dayOfMonth < getMonthDays(year)[monthOfYear];

        int[] monthDays = getMonthDays(year);
        int dayOfYear = dayOfMonth;
        for (int i = 0; i < monthOfYear; i++) {
            dayOfYear += monthDays[i];
        }
        return dayOfYear;
    }
}
