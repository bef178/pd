package pd.time;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static pd.time.TimeExtension.MILLISECONDS_PER_MINUTE;

/**
 * offset between timestamp and local easy-to-read time, in milliseconds
 */
public class SimpleTimeZone implements Comparable<SimpleTimeZone>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Pattern P = Pattern.compile("^[+-]\\d{4}$");

    public static final SimpleTimeZone UTC = new SimpleTimeZone(0);

    /**
     * null is less
     */
    public static int compare(SimpleTimeZone one, SimpleTimeZone another) {
        if (one == another) {
            return 0;
        }
        if (one == null) {
            return -1;
        }
        if (another == null) {
            return 1;
        }
        return SimpleTimeOffset.compare(one.timeOffset, another.timeOffset);
    }

    public static SimpleTimeZone parse(String s) {
        Matcher matcher = P.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException();
        }
        int i = Integer.parseInt(matcher.group(0));
        return new SimpleTimeZone((i / 100 * 60 + i % 100) * MILLISECONDS_PER_MINUTE);
    }

    private final SimpleTimeOffset timeOffset;

    public SimpleTimeZone(long offsetMilliseconds) {
        timeOffset = new SimpleTimeOffset(offsetMilliseconds);
    }

    @Override
    public int compareTo(SimpleTimeZone o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            SimpleTimeZone another = (SimpleTimeZone) obj;
            return this.compareTo(another) == 0;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return timeOffset.hashCode();
    }

    public long getOffsetMilliseconds() {
        return timeOffset.getOffsetMilliseconds();
    }

    @Override
    public String toString() {
        long milliseconds = getOffsetMilliseconds();
        if (milliseconds == 0) {
            return "Z";
        }
        int minutes = (int) (milliseconds / MILLISECONDS_PER_MINUTE);
        return String.format("%+05d", minutes / 60 * 100 + minutes % 60);
    }
}
