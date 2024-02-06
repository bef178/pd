package pd.time;

import java.io.Serializable;

import static pd.time.TimeExtension.MILLISECONDS_PER_SECOND;

/**
 * offset between two timestamps, in milliseconds
 */
public class SimpleTimeOffset implements Comparable<SimpleTimeOffset>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * null is less
     */
    public static int compare(SimpleTimeOffset one, SimpleTimeOffset another) {
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

    private final long offsetMilliseconds;

    public SimpleTimeOffset(long offsetMilliseconds) {
        this.offsetMilliseconds = offsetMilliseconds;
    }

    public SimpleTimeOffset(long startMilliseconds, long endMilliseconds) {
        this(endMilliseconds - startMilliseconds);
    }

    @Override
    public int compareTo(SimpleTimeOffset o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return ((SimpleTimeOffset) obj).offsetMilliseconds == this.offsetMilliseconds;
        }
        return false;
    }

    public long getOffsetMilliseconds() {
        return offsetMilliseconds;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(offsetMilliseconds);
    }

    @Override
    public String toString() {
        long seconds = offsetMilliseconds / MILLISECONDS_PER_SECOND;
        int millisecondOfSecond = (int) (offsetMilliseconds % MILLISECONDS_PER_SECOND);
        return String.format("P%ld.%03d", seconds, millisecondOfSecond);
    }
}
