package pd.time;

import static pd.time.TimeUtil.MILLISECONDS_PER_MINUTE;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;;

/**
 * offset between timestamp and local easy-to-read time
 * TODO daylight saving time should be handled here
 */
public final class TimeZone implements Comparable<TimeZone>, Serializable {

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
        return (int) (one.offsetMilliseconds - another.offsetMilliseconds);
    }

    public static TimeZone fromMilliseconds(long offsetMilliseconds) {
        return new TimeZone(offsetMilliseconds);
    }

    // "+0800" => 480
    public static TimeZone fromString(String s) {
        Matcher matcher = P.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        int i = Integer.parseInt(matcher.group(0));
        return new TimeZone((i / 100 * 60 + i % 100) * MILLISECONDS_PER_MINUTE);
    }

    private final long offsetMilliseconds;

    private TimeZone(long offsetMilliseconds) {
        this.offsetMilliseconds = offsetMilliseconds;
    }

    @Override
    public int compareTo(TimeZone o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return ((TimeZone) obj).offsetMilliseconds == this.offsetMilliseconds;
        }
        return false;
    }

    public long getOffsetMilliseconds() {
        return offsetMilliseconds;
    }

    @Override
    public String toString() {
        int offset = (int) (offsetMilliseconds / MILLISECONDS_PER_MINUTE);
        return String.format("%+05d", offset / 60 * 100 + offset % 60);
    }
}
