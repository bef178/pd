package pd.time;

import java.io.Serializable;;

/**
 * offset between two timestamps, resolution of 1 millisecond
 */
public class TimeOffset implements Comparable<TimeOffset>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * null is less
     */
    public static int compare(TimeOffset one, TimeOffset another) {
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

    public TimeOffset(long offsetMilliseconds) {
        this.offsetMilliseconds = offsetMilliseconds;
    }

    public TimeOffset(long startMilliseconds, long endMilliseconds) {
        this(endMilliseconds - startMilliseconds);
    }

    @Override
    public int compareTo(TimeOffset o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return ((TimeOffset) obj).offsetMilliseconds == this.offsetMilliseconds;
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
        int MILLISECONDS_PER_SECOND = 1000;
        long seconds = offsetMilliseconds / MILLISECONDS_PER_SECOND;
        int millisecondOfSecond = (int) (offsetMilliseconds % MILLISECONDS_PER_SECOND);
        return String.format("P%ld.%03d", seconds, millisecondOfSecond);
    }
}
