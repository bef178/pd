package pd.time;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pd.time.TimeExtension.MILLISECONDS_PER_MINUTE;

class SimpleTimeBuilder implements SimpleTime.Builder {

    private static final Pattern P = Pattern.compile(
            "^(\\d+)-(\\d{2})-(\\d{2})[T ](\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})(Z|( ?([+-]\\d{4})))$");

    public static SimpleTimeBuilder parse(String s, SimpleTimeBuilder builder) {
        Matcher matcher = P.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }

        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2));
        int day = Integer.parseInt(matcher.group(3));
        int hour = Integer.parseInt(matcher.group(4));
        int minute = Integer.parseInt(matcher.group(5));
        int second = Integer.parseInt(matcher.group(6));
        int millisecond = Integer.parseInt(matcher.group(7));
        String zone = matcher.group(8);

        MonthOfYear monthOfYear = MonthOfYear.fromOrdinal(month - 1);
        if (monthOfYear == null) {
            throw new IllegalArgumentException();
        }

        builder.setLocalDatePart(year, monthOfYear, day);
        builder.setLocalTimePart(hour, minute, second, millisecond);

        if (zone.equals("Z")) {
            builder.setTimeZone(SimpleTimeZone.UTC);
        } else {
            int offset = Integer.parseInt(matcher.group(10));
            int offsetMilliseconds = (offset / 100 * 60 + offset % 100) * MILLISECONDS_PER_MINUTE;
            if (offsetMilliseconds == 0) {
                builder.setTimeZone(SimpleTimeZone.UTC);
            } else {
                builder.setTimeZone(new SimpleTimeZone(offsetMilliseconds));
            }
        }

        return builder;
    }

    private int year = 1970;
    private int dayOfYear = 0;
    private int millisecondOfDay = 0;

    private SimpleTimeZone timeZone = null;

    @Override
    public SimpleTime build() {
        long localTotalDays = DateExtension.toDaysSinceEpoch(year, dayOfYear);
        long localTotalMilliseconds = TimeExtension.toMillisecondsSinceEpoch(localTotalDays, millisecondOfDay);
        return new SimpleTime(localTotalMilliseconds, timeZone);
    }

    @Override
    public SimpleTimeBuilder parse(String s) {
        return parse(s, this);
    }

    @Override
    public SimpleTimeBuilder setLocalDatePart(int year, int week, DayOfWeek weekDay) {
        if (week < 0 || week > 52) {
            throw new IllegalArgumentException();
        }
        this.year = year;
        this.dayOfYear = week * 7 + weekDay.ordinal() - DateExtension.findDayOfWeek(DateExtension.toDaysSinceEpoch(year, 0));
        return this;
    }

    @Override
    public SimpleTimeBuilder setLocalDatePart(int year, MonthOfYear month, int day) {
        this.year = year;
        this.dayOfYear = DateExtension.toDayOfYear(year, month.ordinal(), day - 1);
        return this;
    }

    @Override
    public SimpleTimeBuilder setLocalTimePart(int hour, int minute, int second, int millisecond) {
        this.millisecondOfDay = TimeExtension.toMillisecondOfDay(hour, minute, second, millisecond);
        return this;
    }

    @Override
    public SimpleTimeBuilder setLocalMilliseconds(long localMilliseconds) {
        int[] timeComponents = TimeExtension.findTimeComponents(localMilliseconds);
        year = timeComponents[TimeExtension.INDEX_YEAR_OF_TIME];
        dayOfYear = timeComponents[TimeExtension.INDEX_DAY_OF_YEAR];
        millisecondOfDay = timeComponents[TimeExtension.INDEX_MILLISECOND_OF_DAY];
        return this;
    }

    @Override
    public SimpleTimeBuilder setTimeZone(SimpleTimeZone timeZone) {
        this.timeZone = timeZone;
        return this;
    }
}
