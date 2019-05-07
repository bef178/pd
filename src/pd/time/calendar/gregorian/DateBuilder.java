package pd.time.calendar.gregorian;

import pd.time.FastTime;

public final class DateBuilder {

    public static DateBuilder newInstance() {
        return new DateBuilder();
    }

    public static EasyDate toDate2(FastTime fastTime, TimeZone timezone) {
        return new DateAndTimeAndZone2(fastTime, timezone);
    }

    public static EasyDate toDate2(long millisecondsSinceUnixEpoch,
            TimeZone timezone) {
        return toDate2(new FastTime(millisecondsSinceUnixEpoch), timezone);
    }

    private int year = 1970;
    private int dayOfYear = 0;
    private int hh = 0;
    private int mm = 0;
    private int ss = 0;
    private int sss = 0;

    private TimeZone timeZone = TimeZone.UTC;

    private DateBuilder() {
        // private dummy
    }

    public EasyDate build2() {
        return toDate2(toMilliseconds() - timeZone.getMilliseconds(), timeZone);
    }

    public DateBuilder setHour(int hh) {
        assert hh >= 0 && hh < 24;
        this.hh = hh;
        return this;
    }

    public DateBuilder setMillisecond(int sss) {
        assert sss >= 0 && sss < 1000;
        this.sss = sss;
        return this;
    }

    public DateBuilder setMinute(int mm) {
        assert mm >= 0 && mm < 60;
        this.mm = mm;
        return this;
    }

    public DateBuilder setSecond(int ss) {
        assert ss >= 0 && ss < 60;
        this.ss = ss;
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
        this.dayOfYear = DateField.toDayOfYear(year, month - 1, day - 1);
        return this;
    }

    /**
     * week in [0, 52]<br/>
     * day in [0, 6]<br/>
     */
    public DateBuilder setYearAndWeekAndDay(int year, int weekOfYear, int dayOfWeek) {
        assert weekOfYear >= 0 && weekOfYear < 53;
        assert dayOfWeek >= 0 && dayOfWeek < 7;
        int dayOfFirstYearDay = DateField.getDayOfWeek(DateField.toDays(year, 0));
        int dayOfYear = weekOfYear * 7 + dayOfWeek - dayOfFirstYearDay;
        assert dayOfYear >= 0 && dayOfYear < Util.getDaysOfYear(year);
        this.year = year;
        this.dayOfYear = dayOfYear;
        return this;
    }

    private long toMilliseconds() {
        long days = DateField.toDays(year, dayOfYear);
        int millisecondOfDay = TimeField.toMillisecondOfDay(hh, mm, ss, sss);
        return Util.toMilliseconds(days, millisecondOfDay);
    }
}
