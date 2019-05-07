package pd.time.calendar.gregorian;

import pd.time.FastTime;

public abstract class EasyDate {

    private final FastTime fastTime;
    private final TimeZone timeZone;

    EasyDate(final FastTime fastTime, TimeZone timeZone) {
        if (fastTime == null) {
            throw new IllegalArgumentException();
        }
        if (timeZone == null) {
            throw new IllegalArgumentException();
        }
        this.fastTime = fastTime;
        this.timeZone = timeZone;
    }

    public DateBuilder builder() {
        return DateBuilder.newInstance();
    }

    @Override
    public final boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            EasyDate a = (EasyDate) obj;
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
    public abstract int getField(DateField field);

    public abstract int getField(TimeField field);

    public final TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public final int hashCode() {
        return getFastTime().hashCode() * 31 + getTimeZone().hashCode();
    }

    public abstract EasyDate rebase(TimeZone timeZone);

    @Override
    public final String toString() {
        return String.format("%04d-%02d-%02d %02d:%02d:%02d.%03d %s",
                getField(DateField.YEAR), getField(DateField.MONTH_OF_YEAR),
                getField(DateField.DAY_OF_MONTH), getField(TimeField.HOUR),
                getField(TimeField.MINUTE), getField(TimeField.SECOND),
                getField(TimeField.MILLISECOND), getTimeZone().toString());
    }
}
