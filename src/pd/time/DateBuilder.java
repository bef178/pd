package pd.time;

import static pd.time.Ctime.MILLISECONDS_PER_MINUTE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pd.time.Ctime.TimeField;

final class DateBuilder implements EasyTime.Builder {

    private static final Pattern P = Pattern.compile(
            "^(\\d+)-(\\d{2})-(\\d{2}) (\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3}) ((\\+|-)\\d{4})$");

    public static DateBuilder fromString(String s, DateBuilder builder) {
        Matcher matcher = P.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        int hh = Integer.parseInt(matcher.group(4));
        int mm = Integer.parseInt(matcher.group(5));
        int ss = Integer.parseInt(matcher.group(6));
        int sss = Integer.parseInt(matcher.group(7));
        int offset = Integer.parseInt(matcher.group(8));

        builder.setLocalDatePart(year, MonthOfYear.fromOrdinal(month - 1), day);
        builder.setLocalTimePart(hh, mm, ss, sss);

        int offsetMilliseconds = (offset / 100 * 60 + offset % 100) * MILLISECONDS_PER_MINUTE;
        builder.setTimeZone(new ZoneTimeOffset(offsetMilliseconds));

        return builder;
    }

    private int year = 1970;
    private int dayOfYear = 0;
    private int millisecondOfDay = 0;

    private ZoneTimeOffset timeZone = null;

    @Override
    public EasyTime build() {
        long localTotalDays = Ctime.totalDays(year, dayOfYear);
        long localTotalMilliseconds = Ctime.totalMilliseconds(localTotalDays,
                millisecondOfDay);
        return new EasyTime(localTotalMilliseconds, timeZone);
    }

    @Override
    public DateBuilder fillByString(String s) {
        return fromString(s, this);
    }

    @Override
    public DateBuilder setLocalDatePart(int year, int week, DayOfWeek day) {
        assert week >= 0 && week <= 52;
        int weekOfYear = week;
        int dayOfWeek = day.ordinal();

        int dayOfFirstYearDay = Ctime.daysToDayOfWeek(Ctime.totalDays(year, 0));
        int dayOfYear = weekOfYear * 7 + dayOfWeek - dayOfFirstYearDay;
        assert dayOfYear >= 0 && dayOfYear < Ctime.getNumDaysByYear(year);
        this.year = year;
        this.dayOfYear = dayOfYear;
        return this;
    }

    /**
     * day in [1, 31]<br/>
     */
    @Override
    public DateBuilder setLocalDatePart(int year, MonthOfYear month, int day) {
        assert day >= 1 && day <= 31;
        int monthOfYear = month.ordinal();
        int dayOfMonth = day - 1;

        this.year = year;
        this.dayOfYear = Ctime.toDayOfYear(year, monthOfYear, dayOfMonth);
        return this;
    }

    @Override
    public DateBuilder setLocalTimePart(int hh, int mm, int ss, int sss) {
        assert hh >= 0 && hh < 24;
        assert mm >= 0 && mm < 60;
        assert ss >= 0 && ss <= 60;
        assert sss >= 0 && sss < 1000;
        this.millisecondOfDay = Ctime.toMillisecondOfDay(hh, mm, ss, sss);
        return this;
    }

    @Override
    public DateBuilder setLocalTotalMilliseconds(long localTotalMilliseconds) {
        int[] values = Ctime.breakMilliseconds(localTotalMilliseconds);
        year = values[TimeField.YEAR.ordinal()];
        dayOfYear = values[TimeField.DAY_OF_YEAR.ordinal()];
        millisecondOfDay = values[TimeField.MILLISECONDS_OF_DAY.ordinal()];
        return this;
    }

    @Override
    public DateBuilder setTimeZone(ZoneTimeOffset timeZone) {
        this.timeZone = timeZone;
        return this;
    }
}
