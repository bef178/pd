package pd.time;

import pd.time.TimeUtil.DateField;
import pd.time.TimeUtil.TimeField;

/**
 * A "local" date and time with time zone
 */
public class EasyTime {

    public static interface Builder {

        public EasyTime build();

        public Builder setLocalTimeFields(int hour, int minute, int second, int millisecond);

        public Builder setTimeZone(TimeZone timeZone);

        /**
         * day in [1, 31]<br/>
         */
        public Builder setLocalDateFields(int year, MonthOfYear month, int day);

        /**
         * week in [0, 52]<br/>
         */
        public Builder setLocalDateFields(int year, int week, DayOfWeek day);
    }

    private final FastTime fastTime;
    private final TimeZone timeZone;

    private transient int[] dateFields;
    private transient int[] timeFields;

    EasyTime(final FastTime fastTime, TimeZone timeZone) {
        if (fastTime == null) {
            throw new IllegalArgumentException();
        }
        if (timeZone == null) {
            throw new IllegalArgumentException();
        }
        this.fastTime = fastTime;
        this.timeZone = timeZone;

        updateFields();
    }

    public static Builder builder() {
        return new DateBuilder();
    }

    @Override
    public final boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && o.getClass() == this.getClass()) {
            EasyTime a = (EasyTime) o;
            return this.getFastTime().equals(a.getFastTime())
                    && this.getTimeZone().equals(a.getTimeZone());
        }
        return false;
    }

    public final FastTime getFastTime() {
        return fastTime;
    }

    /**
     * year in range of int32<br/>
     * day_of_year in [1, 366]<br/>
     * month_of_year in [1, 12]<br/>
     * day_of_month in [1, 31]<br/>
     * week_of_year in [0, 52], week 0 covers the first day through the first Saturday<br/>
     * day_of_week in [0, 6], 0 for Sunday<br/>
     */
    public int getField(DateField field) {
        if (field != null) {
            updateFields();
            switch (field) {
            case YEAR:
                return dateFields[0];
            case DAY_OF_YEAR:
                return dateFields[1] + 1;
            case MONTH_OF_YEAR:
                return dateFields[2] + 1;
            case DAY_OF_MONTH:
                return dateFields[3] + 1;
            case WEEK_OF_YEAR:
                return dateFields[4];
            case DAY_OF_WEEK:
                return dateFields[5];
            default:
                break;
            }
        }
        throw new IllegalArgumentException();
    }

    public int getField(TimeField field) {
        if (field != null) {
            updateFields();
            switch (field) {
            case HOUR:
                return timeFields[0];
            case MINUTE:
                return timeFields[1];
            case SECOND:
                return timeFields[2];
            case MILLISECOND:
                return timeFields[3];
            default:
                break;
            }
        }
        throw new IllegalArgumentException();
    }

    public final TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public final int hashCode() {
        return getFastTime().hashCode() * 31 + getTimeZone().hashCode();
    }

    public EasyTime rebase(TimeZone timeZone) {
        assert timeZone != null;
        if (this.getTimeZone().equals(timeZone)) {
            return this;
        }
        return new EasyTime(getFastTime(), timeZone);
    }

    @Override
    public final String toString() {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d %s",
                getField(DateField.YEAR), getField(DateField.MONTH_OF_YEAR), getField(DateField.DAY_OF_MONTH),
                getField(TimeField.HOUR), getField(TimeField.MINUTE), getField(TimeField.SECOND),
                getField(TimeField.MILLISECOND),
                getTimeZone().toString());
    }

    private void updateFields() {
        if (dateFields != null && timeFields != null) {
            return;
        }

        long milliseconds = getFastTime().getMilliseconds() + getTimeZone().getMilliseconds();

        int millisecondOfDay = TimeUtil.millisecondsToMillisecondOfDay(milliseconds);
        timeFields = TimeUtil.millisecondOfDayToFields(millisecondOfDay);

        long days = TimeUtil.millisecondsToDays(milliseconds, millisecondOfDay);
        dateFields = TimeUtil.daysToFields(days);
    }
}
