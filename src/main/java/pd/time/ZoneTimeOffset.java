package pd.time;

import static pd.time.Ctime.MILLISECONDS_PER_MINUTE;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;;

/**
 * offset between timestamp and local easy-to-read time
 */
public final class ZoneTimeOffset implements Comparable<ZoneTimeOffset>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Pattern P = Pattern.compile("^(\\+|-)\\d{4}$");

    public static final ZoneTimeOffset UTC = new ZoneTimeOffset(0);

    /**
     * null is less
     */
    public static int compare(ZoneTimeOffset one, ZoneTimeOffset another) {
        if (one == another) {
            return 0;
        }
        if (one == null) {
            return -1;
        }
        if (another == null) {
            return 1;
        }
        return TimeOffset.compare(one.timeOffset, another.timeOffset);
    }

    // "+0800" => 480
    public static ZoneTimeOffset fromString(String s) {
        Matcher matcher = P.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        int i = Integer.parseInt(matcher.group(0));
        return new ZoneTimeOffset((i / 100 * 60 + i % 100) * MILLISECONDS_PER_MINUTE);
    }

    private final TimeOffset timeOffset;

    public ZoneTimeOffset(long offsetMilliseconds) {
        timeOffset = new TimeOffset(offsetMilliseconds);
    }

    @Override
    public int compareTo(ZoneTimeOffset o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            ZoneTimeOffset o = (ZoneTimeOffset) obj;
            return TimeOffset.compare(o.timeOffset, this.timeOffset) == 0;
        }
        return false;
    }

    public long getOffsetMilliseconds() {
        return timeOffset.getOffsetMilliseconds();
    }

    @Override
    public String toString() {
        int offset = (int) (getOffsetMilliseconds() / MILLISECONDS_PER_MINUTE);
        return String.format("%+05d", offset / 60 * 100 + offset % 60);
    }
}
