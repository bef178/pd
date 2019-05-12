package pd.time.calendar.gregorian;

import pd.time.FastTime;

public final class DateBuilder {

    public static DateBuilder newInstance() {
        return new DateBuilder();
    }

    public static EasyTime toDate2(FastTime fastTime, TimeZone timezone) {
        return new DateAndTimeAndZone2(fastTime, timezone);
    }

    public static EasyTime toDate2(long millisecondsSinceUnixEpoch,
            TimeZone timezone) {
        return toDate2(new FastTime(millisecondsSinceUnixEpoch), timezone);
    }

    private int year = 1970;
    private int dayOfYear = 0;
    private int millisecondOfDay = 0;

    private TimeZone timeZone = TimeZone.UTC;

    private DateBuilder() {
        // private dummy
    }

    public EasyTime build2() {
        return toDate2(toMilliseconds() - timeZone.getMilliseconds(), timeZone);
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
