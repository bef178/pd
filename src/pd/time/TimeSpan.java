package pd.time;

import java.io.Serializable;

/**
 * Represents a period of time basically in seconds, could be negative.
 */
public final class TimeSpan implements Comparable<TimeSpan>, Serializable {

    public static final int MILLISECONDS_PER_SECOND = 1000;

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * null is less
     */
    public static int compare(TimeSpan one, TimeSpan another) {
        if (one == another) {
            return 0;
        }
        if (one == null) {
            return -1;
        }
        if (another == null) {
            return 1;
        }
        return (int) (one.getMilliseconds() - another.getMilliseconds());
    }

    public static TimeSpan fromMilliseconds(long milliseconds) {
        return new TimeSpan(milliseconds);
    }

    public static TimeSpan fromSeconds(long seconds) {
        // FIXME overflow
        return new TimeSpan(MILLISECONDS_PER_SECOND * seconds);
    }

    // 2^63 = 9.22e18
    // int64 as ms: 9.22e15s = 1.06e11d = 2.92e8y
    private final long s3;

    private TimeSpan(long milliseconds) {
        this.s3 = milliseconds;
    }

    public TimeSpan addMilliseconds(long milliseconds) {
        return new TimeSpan(this.s3 + milliseconds);
    }

    public TimeSpan addSeconds(long seconds) {
        return addMilliseconds(MILLISECONDS_PER_SECOND * seconds);
    }

    @Override
    public int compareTo(TimeSpan o) {
        return compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return ((TimeSpan) obj).s3 == this.s3;
        }
        return false;
    }

    /**
     * milliseconds as difference, negative ok
     */
    public long getMilliseconds() {
        return s3;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(s3);
    }

    @Override
    public String toString() {
        double seconds = 1.0d * s3 / MILLISECONDS_PER_SECOND;
        return String.format("P%.03f", seconds);
    }
}
