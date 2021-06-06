package pd.time;

import static pd.time.Ctime.MILLISECONDS_PER_SECOND;

import pd.time.Ctime.TimeField;

/**
 * A "local" date and time with time zone
 */
public class EasyTime {

    public static interface Builder {

        public EasyTime build();

        public Builder fillByString(String s);

        /**
         * week in [0, 52]<br/>
         */
        public Builder setLocalDatePart(int year, int week, DayOfWeek day);

        /**
         * day in [1, 31]<br/>
         */
        public Builder setLocalDatePart(int year, MonthOfYear month, int day);

        public Builder setLocalTimePart(int hour, int minute, int second, int millisecond);

        public Builder setLocalTotalMilliseconds(long localTotalMilliseconds);

        public Builder setTimeZone(ZoneTimeOffset timeZone);
    }

    public static Builder builder() {
        return new DateBuilder();
    }

    public static EasyTime now() {
        long localTotalMilliseconds = System.currentTimeMillis();
        long offsetMilliseconds = java.util.TimeZone.getDefault().getRawOffset();
        return new EasyTime(localTotalMilliseconds, offsetMilliseconds);
    }

    private final long localTotalMilliseconds;

    private final ZoneTimeOffset zoneTimeOffset;

    private transient int[] fieldValues;

    private EasyTime(long localTotalMilliseconds, long offsetMilliseconds) {
        this(localTotalMilliseconds, new ZoneTimeOffset(offsetMilliseconds));
    }

    EasyTime(long localTotalMilliseconds, ZoneTimeOffset timeZone) {
        this.localTotalMilliseconds = localTotalMilliseconds;
        this.zoneTimeOffset = timeZone;
    }

    public EasyTime addMilliseconds(long milliseconds) {
        return new EasyTime(localTotalMilliseconds + milliseconds, zoneTimeOffset);
    }

    public EasyTime addSeconds(long seconds) {
        return addMilliseconds(seconds * MILLISECONDS_PER_SECOND);
    }

    /**
     * the timestamp will probably change
     */
    public EasyTime assign(ZoneTimeOffset timeZone) {
        if (ZoneTimeOffset.compare(this.getTimeZone(), timeZone) == 0) {
            return this;
        }
        return new EasyTime(localTotalMilliseconds, timeZone);
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && o.getClass() == this.getClass()) {
            EasyTime a = (EasyTime) o;
            return this.localTotalMilliseconds == a.localTotalMilliseconds
                    && ZoneTimeOffset.compare(zoneTimeOffset, a.zoneTimeOffset) == 0;
        }
        return false;
    }

    /**
     * year in range of int32<br/>
     * day_of_year in [1, 366]<br/>
     * month_of_year in [1, 12]<br/>
     * day_of_month in [1, 31]<br/>
     * week_of_year in [0, 52], week 0 covers the first day through the first Saturday<br/>
     * day_of_week in [0, 6], 0 for Sunday<br/>
     */
    public int getFieldValue(TimeField field) {
        if (field == null) {
            throw new IllegalArgumentException();
        }

        if (fieldValues == null) {
            fieldValues = Ctime.breakMilliseconds(localTotalMilliseconds);
        }

        switch (field) {
            case DAY_OF_YEAR:
            case MONTH_OF_YEAR:
            case DAY_OF_MONTH:
                return fieldValues[field.ordinal()] + 1;
            default:
                return fieldValues[field.ordinal()];
        }
    }

    public final long getTotalLocalMilliseconds() {
        return localTotalMilliseconds;
    }

    public final long getMillisecondsSinceEpoch() {
        if (getTimeZone() == null) {
            throw new UnsupportedOperationException();
        }
        return localTotalMilliseconds - getTimeZone().getOffsetMilliseconds();
    }

    public final ZoneTimeOffset getTimeZone() {
        return zoneTimeOffset;
    }

    @Override
    public final int hashCode() {
        return Long.hashCode(localTotalMilliseconds) * 31
                + (zoneTimeOffset == null ? 0 : zoneTimeOffset.hashCode());
    }

    /**
     * the timestamp will not change
     */
    public EasyTime rebase(ZoneTimeOffset timeZone) {
        if (ZoneTimeOffset.compare(this.zoneTimeOffset, timeZone) == 0) {
            return this;
        }
        if (this.zoneTimeOffset == null) {
            throw new UnsupportedOperationException();
        }
        if (timeZone == null) {
            throw new IllegalArgumentException();
        }
        return new EasyTime(getMillisecondsSinceEpoch() + timeZone.getOffsetMilliseconds(),
                timeZone);
    }

    @Override
    public final String toString() {
        if (fieldValues == null) {
            fieldValues = Ctime.breakMilliseconds(localTotalMilliseconds);
        }
        return zoneTimeOffset == null || zoneTimeOffset == ZoneTimeOffset.UTC
                ? Ctime.toUtcString(fieldValues)
                : String.format("%04d-%02d-%02dT%02d:%02d:%02d.%03d%s",
                        getFieldValue(TimeField.YEAR),
                        getFieldValue(TimeField.MONTH_OF_YEAR),
                        getFieldValue(TimeField.DAY_OF_MONTH),
                        getFieldValue(TimeField.HH),
                        getFieldValue(TimeField.MM),
                        getFieldValue(TimeField.SS),
                        getFieldValue(TimeField.SSS),
                        getTimeZone().toString());
    }
}
