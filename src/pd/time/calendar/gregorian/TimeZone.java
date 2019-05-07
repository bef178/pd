package pd.time.calendar.gregorian;

import static pd.time.calendar.gregorian.Util.MILLISECONDS_PER_MINUTE;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;;

public final class TimeZone implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Pattern P = Pattern.compile("^(\\+|-)\\d{4}$");

    public static final TimeZone UTC = new TimeZone(0);

    /**
     * null is less
     */
    public static int compare(TimeZone one, TimeZone another) {
        if (one == another) {
            return 0;
        }
        if (one == null) {
            return -1;
        }
        if (another == null) {
            return 1;
        }
        return one.getMilliseconds() - another.getMilliseconds();
    }

    // "+0800" => 480
    public static TimeZone from(String timezone) {
        Matcher matcher = P.matcher(timezone);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        int i = Integer.parseInt(matcher.group(0));
        return new TimeZone((i / 100 * 60 + i % 100) * MILLISECONDS_PER_MINUTE);
    }

    private final int s3;

    private TimeZone(int offsetMilliseconds) {
        s3 = offsetMilliseconds;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return ((TimeZone) obj).s3 == this.s3;
        }
        return false;
    }

    public int getMilliseconds() {
        return s3;
    }

    @Override
    public String toString() {
        int offset = getMilliseconds() / MILLISECONDS_PER_MINUTE;
        return String.format("%+4d", offset / 60 * 100 + offset % 60);
    }
}
