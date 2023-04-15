package pd.time;

import static pd.time.TimeExtension.MILLISECONDS_PER_SECOND;
import static pd.time.TimeExtension.findTimeComponents;

/**
 * local time with time zone
 */
public class SimpleTime {

    public interface Builder {

        SimpleTime build();

        Builder parse(String s);

        /**
         * week in [0,52]
         */
        Builder setLocalDatePart(int year, int week, DayOfWeek weekDay);

        /**
         * day in [1,31]
         */
        Builder setLocalDatePart(int year, MonthOfYear month, int day);

        Builder setLocalTimePart(int hour, int minute, int second, int millisecond);

        Builder setLocalMilliseconds(long localMilliseconds);

        Builder setTimeZone(SimpleTimeZone timeZone);
    }

    public static Builder builder() {
        return new SimpleTimeBuilder();
    }

    public static SimpleTime now() {
        long localMilliseconds = System.currentTimeMillis();
        long offsetMilliseconds = java.util.TimeZone.getDefault().getRawOffset();
        return new SimpleTime(localMilliseconds, offsetMilliseconds);
    }

    private final long localMilliseconds;

    private final SimpleTimeZone timeZone;

    private transient int[] localTimeComponents;

    private SimpleTime(long localMilliseconds, long offsetMilliseconds) {
        this(localMilliseconds, new SimpleTimeZone(offsetMilliseconds));
    }

    SimpleTime(long localMilliseconds, SimpleTimeZone timeZone) {
        this.localMilliseconds = localMilliseconds;
        this.timeZone = timeZone;
    }

    public SimpleTime addMilliseconds(long milliseconds) {
        return new SimpleTime(localMilliseconds + milliseconds, timeZone);
    }

    public SimpleTime addSeconds(long seconds) {
        return addMilliseconds(seconds * MILLISECONDS_PER_SECOND);
    }

    /**
     * the timestamp will probably change
     */
    public SimpleTime assign(SimpleTimeZone timeZone) {
        if (SimpleTimeZone.compare(this.getTimeZone(), timeZone) == 0) {
            return this;
        }
        return new SimpleTime(localMilliseconds, timeZone);
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && o.getClass() == this.getClass()) {
            SimpleTime another = (SimpleTime) o;
            return this.localMilliseconds == another.localMilliseconds
                    && SimpleTimeZone.compare(timeZone, another.timeZone) == 0;
        }
        return false;
    }

    public int getYear() {
        return getLocalTimeComponent(TimeExtension.INDEX_YEAR_OF_TIME);
    }

    public MonthOfYear getMonth() {
        int i = getLocalTimeComponent(TimeExtension.INDEX_MONTH_OF_YEAR);
        return MonthOfYear.fromOrdinal(i);
    }

    /**
     * [1,31]
     */
    public int getDay() {
        return getLocalTimeComponent(TimeExtension.INDEX_DAY_OF_MONTH) + 1;
    }

    public int getHour() {
        return getLocalTimeComponent(TimeExtension.INDEX_HOUR_OF_DAY);
    }

    public int getMinute() {
        return getLocalTimeComponent(TimeExtension.INDEX_MINUTE_OF_HOUR);
    }

    public int getSecond() {
        return getLocalTimeComponent(TimeExtension.INDEX_SECOND_OF_MINUTE);
    }

    public int getMillisecond() {
        return getLocalTimeComponent(TimeExtension.INDEX_MILLISECOND_OF_SECOND);
    }

    public DayOfWeek getWeekDay() {
        int i = getLocalTimeComponent(TimeExtension.INDEX_DAY_OF_WEEK);
        return DayOfWeek.fromOrdinal(i);
    }

    private int getLocalTimeComponent(int index) {
        if (localTimeComponents == null) {
            localTimeComponents = findTimeComponents(localMilliseconds);
        }
        if (index < 0 || index >= localTimeComponents.length) {
            throw new IllegalArgumentException();
        }
        return localTimeComponents[index];
    }

    public long getLocalMilliseconds() {
        return localMilliseconds;
    }

    public long getMillisecondsSinceEpoch() {
        if (getTimeZone() == null) {
            throw new UnsupportedOperationException("Time zone not set");
        }
        return localMilliseconds - getTimeZone().getOffsetMilliseconds();
    }

    public SimpleTimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(localMilliseconds) * 31
                + (timeZone == null ? 0 : timeZone.hashCode());
    }

    /**
     * the timestamp will not change
     */
    public SimpleTime rebase(SimpleTimeZone timeZone) {
        if (SimpleTimeZone.compare(this.timeZone, timeZone) == 0) {
            return this;
        }
        if (this.timeZone == null) {
            throw new UnsupportedOperationException();
        }
        if (timeZone == null) {
            throw new IllegalArgumentException();
        }
        return new SimpleTime(getMillisecondsSinceEpoch() + timeZone.getOffsetMilliseconds(),
                timeZone);
    }

    @Override
    public final String toString() {
        return String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03d%s",
                getYear(),
                getMonth().ordinal() + 1,
                getDay(),
                getHour(),
                getMinute(),
                getSecond(),
                getMillisecond(),
                this.timeZone != null ? this.timeZone : "");
    }
}
