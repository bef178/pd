package pd.time.calendar.gregorian;

import static pd.time.calendar.gregorian.DateField.Y1;
import static pd.time.calendar.gregorian.DateField.Y100;
import static pd.time.calendar.gregorian.DateField.Y4;
import static pd.time.calendar.gregorian.DateField.Y400;
import static pd.time.calendar.gregorian.DateField.getDayOfWeek;
import static pd.time.calendar.gregorian.Util.getMonthDays;

import pd.time.FastTime;

/**
 * A "local" date and time with time zone on Gregorian calendar.
 */
final class DateAndTimeAndZone2 extends EasyDate {

    private final int year;
    private final int dayOfYear;
    private final int monthOfYear;
    private final int dayOfMonth;
    private final int weekOfYear;
    private final int dayOfWeek;

    private final int hh;
    private final int mm;
    private final int ss;
    private final int sss;

    DateAndTimeAndZone2(final FastTime fastTime, TimeZone timeZone) {
        super(fastTime, timeZone);

        long milliseconds = fastTime.getMilliseconds() + timeZone.getMilliseconds();

        int millisecondOfDay = Util.getMillisecondOfDay(milliseconds);
        this.hh = TimeField.get(millisecondOfDay, TimeField.HOUR);
        this.mm = TimeField.get(millisecondOfDay, TimeField.MINUTE);
        this.ss = TimeField.get(millisecondOfDay, TimeField.SECOND);
        this.sss = TimeField.get(millisecondOfDay, TimeField.MILLISECOND);

        long days = Util.getDays(milliseconds, millisecondOfDay);
        assert days >= -784353015833L && days < 784351576777L; // year in range of int32

        this.dayOfWeek = getDayOfWeek(days);

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

        this.year = 2000 + 400 * n400 + 100 * n100 + 4 * n4 + 1 * n1;

        this.dayOfYear = j;

        this.weekOfYear = (getDayOfWeek(days - dayOfYear) + dayOfYear) / 7;

        int[] monthDays = getMonthDays(year);

        int monthOfYear = 0;
        while (j >= monthDays[monthOfYear]) {
            j -= monthDays[monthOfYear];
            monthOfYear++;
        }

        this.monthOfYear = monthOfYear;

        this.dayOfMonth = j;
    }

    @Override
    public int getField(DateField field) {
        switch (field) {
            case YEAR:
                return year;
            case DAY_OF_YEAR:
                return dayOfYear + 1;
            case MONTH_OF_YEAR:
                return monthOfYear + 1;
            case DAY_OF_MONTH:
                return dayOfMonth + 1;
            case WEEK_OF_YEAR:
                return weekOfYear;
            case DAY_OF_WEEK:
                return dayOfWeek;
            default:
                break;
        }
        return -1;
    }

    @Override
    public int getField(TimeField field) {
        switch (field) {
            case HOUR:
                return hh;
            case MINUTE:
                return mm;
            case SECOND:
                return ss;
            case MILLISECOND:
                return sss;
            default:
                break;
        }
        return -1;
    }

    @Override
    public DateAndTimeAndZone2 rebase(TimeZone timeZone) {
        assert timeZone != null;
        if (this.getTimeZone().equals(timeZone)) {
            return this;
        }
        return new DateAndTimeAndZone2(getFastTime(), timeZone);
    }
}
