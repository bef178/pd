package pd.time.calendar.gregorian;

import static pd.time.calendar.gregorian.Util.getDaysOfYear;
import static pd.time.calendar.gregorian.Util.getMonthDays;

public enum DateField {

    YEAR, DAY_OF_YEAR, MONTH_OF_YEAR, DAY_OF_MONTH, WEEK_OF_YEAR, DAY_OF_WEEK;

    static final int Y1 = 365;
    static final int Y4 = Y1 * 4 + 1; // 1461
    static final int Y100 = Y4 * 25 - 1; // 36524
    static final int Y400 = Y100 * 4 + 1; // 146097

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

    static int toDayOfYear(final int year, final int monthOfYear, final int dayOfMonth) {
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
