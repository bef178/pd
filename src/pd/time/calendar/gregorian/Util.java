package pd.time.calendar.gregorian;

final class Util {

    static final int MILLISECONDS_PER_SECOND = 1000;

    static final int MILLISECONDS_PER_MINUTE = MILLISECONDS_PER_SECOND * 60;

    static final int MILLISECONDS_PER_HOUR = MILLISECONDS_PER_MINUTE * 60;

    static final int MILLISECONDS_PER_DAY = MILLISECONDS_PER_HOUR * 24;

    private static final int[] MONTH_DAYS = {
            31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private static final int[] MONTH_DAYS_366 = {
            31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    static long getDays(long milliseconds) {
        int millisecondOfDay = getMillisecondOfDay(milliseconds);
        return (milliseconds - millisecondOfDay) / MILLISECONDS_PER_DAY;
    }

    static long getDays(long milliseconds, int millisecondOfDay) {
        return (milliseconds - millisecondOfDay) / MILLISECONDS_PER_DAY;
    }

    static int getDaysOfYear(int year) {
        return isLeapYear(year) ? 366 : 365;
    }

    /**
     * must be positive or 0
     */
    static int getMillisecondOfDay(long milliseconds) {
        milliseconds = milliseconds % MILLISECONDS_PER_DAY;
        if (milliseconds < 0) {
            milliseconds += MILLISECONDS_PER_DAY;
        }
        return (int) milliseconds;
    }

    static int[] getMonthDays(int year) {
        return isLeapYear(year) ? MONTH_DAYS_366 : MONTH_DAYS;
    }

    private static boolean isLeapYear(int year) {
        if (year % 100 == 0) {
            return year % 400 == 0;
        }
        return year % 4 == 0;
    }

    static long toMilliseconds(long days, int millisecondOfDay) {
        assert millisecondOfDay >= 0 && millisecondOfDay < MILLISECONDS_PER_DAY;
        return MILLISECONDS_PER_DAY * days + millisecondOfDay;
    }

    private Util() {
        // private dummy
    }
}
