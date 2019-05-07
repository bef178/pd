package pd.time.calendar.gregorian;

import static pd.time.calendar.gregorian.Util.MILLISECONDS_PER_HOUR;
import static pd.time.calendar.gregorian.Util.MILLISECONDS_PER_MINUTE;
import static pd.time.calendar.gregorian.Util.MILLISECONDS_PER_SECOND;

public enum TimeField {

    HOUR, MINUTE, SECOND, MILLISECOND;

    static int get(final int millisecondOfDay, final TimeField field) {
        switch (field) {
            case HOUR:
                return (millisecondOfDay / MILLISECONDS_PER_HOUR) % 24;
            case MINUTE:
                return (millisecondOfDay / MILLISECONDS_PER_MINUTE) % 60;
            case SECOND:
                return (millisecondOfDay / MILLISECONDS_PER_SECOND) % 60;
            case MILLISECOND:
                return millisecondOfDay % MILLISECONDS_PER_SECOND;
            default:
                break;
        }
        return -1;
    }

    static int toMillisecondOfDay(int hh, int mm, int ss, int sss) {
        assert hh >= 0 && hh < 60;
        assert mm >= 0 && mm < 60;
        assert ss >= 0 && ss < 60;
        assert sss >= 0 && sss < 1000;
        return MILLISECONDS_PER_HOUR * hh + MILLISECONDS_PER_MINUTE * mm
                + MILLISECONDS_PER_SECOND * ss + sss;
    }
}
