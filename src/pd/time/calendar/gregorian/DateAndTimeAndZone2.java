package pd.time.calendar.gregorian;

import pd.time.FastTime;
import pd.time.calendar.gregorian.TimeUtil.DateField;
import pd.time.calendar.gregorian.TimeUtil.TimeField;

final class DateAndTimeAndZone2 extends EasyTime {

    private transient int[] dateFields;
    private transient int[] timeFields;

    DateAndTimeAndZone2(final FastTime fastTime, TimeZone timeZone) {
        super(fastTime, timeZone);
        updateFields();
    }

    @Override
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

    @Override
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

    @Override
    public DateAndTimeAndZone2 rebase(TimeZone timeZone) {
        assert timeZone != null;
        if (this.getTimeZone().equals(timeZone)) {
            return this;
        }
        return new DateAndTimeAndZone2(getFastTime(), timeZone);
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
