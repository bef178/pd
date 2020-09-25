package pd.time.calendar.gregorian;

import static pd.time.calendar.gregorian.TimeUtil.MILLISECONDS_PER_MINUTE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pd.time.FastTime;

public final class DateBuilder {

    private static final Pattern P = Pattern.compile(
            "^(\\d+)-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3}) ((\\+|-)\\d{4})$");

    public static DateBuilder newInstance() {
        return new DateBuilder();
    }

    public static EasyTime toDate2(FastTime fastTime, TimeZone timeZone) {
        return new DateAndTimeAndZone2(fastTime, timeZone);
    }

    public static EasyTime toDate2(String s) {
        Matcher matcher = P.matcher(s);
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
        int offset = Integer.parseInt(matcher.group(8));

        long days = TimeUtil.toDays(year, TimeUtil.toDayOfYear(year, monthOfYear, dayOfMonth));
        int millisecondOfDay = TimeUtil.toMillisecondOfDay(hh, mm, ss, sss);
        long milliseconds = TimeUtil.toMilliseconds(days, millisecondOfDay);

        int offsetMilliseconds = (offset / 100 * 60 + offset % 100) * MILLISECONDS_PER_MINUTE;

        return DateBuilder.toDate2(FastTime.fromMilliseconds(milliseconds - offsetMilliseconds),
                TimeZone.fromMilliseconds(offsetMilliseconds));
    }

    private int year = 1970;
    private int dayOfYear = 0;
    private int millisecondOfDay = 0;

    private TimeZone timeZone = TimeZone.UTC;

    private DateBuilder() {
        // private dummy
    }

    public EasyTime build2() {
        return toDate2(FastTime.fromMilliseconds(toMilliseconds() - timeZone.getMilliseconds()),
                timeZone);
    }

    public DateBuilder setTimeFields(int hour, int minute, int second, int millisecond) {
        assert hour >= 0 && hour < 24;
        assert minute >= 0 && minute < 60;
        assert second >= 0 && second <= 60;
        assert millisecond >= 0 && millisecond < 1000;
        this.millisecondOfDay = TimeUtil.toMillisecondOfDay(hour, minute, second, millisecond);
        return this;
    }

    public DateBuilder setTimeZone(TimeZone timeZone) {
        assert timeZone != null;
        this.timeZone = timeZone;
        return this;
    }

    /**
     * month in [1, 12]<br/>
     * day in [1, 31]<br/>
     */
    public DateBuilder setYearAndMonthAndDay(int year, int month, int day) {
        this.year = year;
        this.dayOfYear = TimeUtil.toDayOfYear(year, month - 1, day - 1);
        return this;
    }

    /**
     * week in [0, 52]<br/>
     * day in [0, 6]<br/>
     */
    public DateBuilder setYearAndWeekAndDay(int year, int weekOfYear, int dayOfWeek) {
        assert weekOfYear >= 0 && weekOfYear < 53;
        assert dayOfWeek >= 0 && dayOfWeek < 7;
        int dayOfFirstYearDay = TimeUtil.daysToDayOfWeek(TimeUtil.toDays(year, 0));
        int dayOfYear = weekOfYear * 7 + dayOfWeek - dayOfFirstYearDay;
        assert dayOfYear >= 0 && dayOfYear < TimeUtil.daysPerYear(year);
        this.year = year;
        this.dayOfYear = dayOfYear;
        return this;
    }

    private long toMilliseconds() {
        long days = TimeUtil.toDays(year, dayOfYear);
        return TimeUtil.toMilliseconds(days, millisecondOfDay);
    }
}
