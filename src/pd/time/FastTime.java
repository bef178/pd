package pd.time;

import java.io.Serializable;

/**
 * Essentially a time span, resolution of 1 millisecond.
 * Also able to represent a time point started from Unix Epoch (1970-01-01 00:00 +0000).
 */
public final class FastTime implements Comparable<FastTime>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final int MILLISECONDS_PER_SECOND = 1000;

    public static final FastTime UnixEpoch = new FastTime(0);

    /**
     * null is less
     */
    public static int compare(FastTime one, FastTime another) {
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

    public static FastTime fromMilliseconds(long milliseconds) {
        return new FastTime(milliseconds);
    }

    public static FastTime fromSeconds(long seconds) {
        // FIXME overflow
        return new FastTime(MILLISECONDS_PER_SECOND * seconds);
    }

    public static FastTime now() {
        return new FastTime(System.currentTimeMillis());
    }

    // 2^63ms = 9.22e18ms = 9.22e15s = 1.06e11d = 2.92e8y
    private final long s3;

    private FastTime(long milliseconds) {
        this.s3 = milliseconds;
    }

    public FastTime addMilliseconds(long milliseconds) {
        return new FastTime(this.s3 + milliseconds);
    }

    public FastTime addSeconds(long seconds) {
        return addMilliseconds(MILLISECONDS_PER_SECOND * seconds);
    }

    @Override
    public int compareTo(FastTime another) {
        return FastTime.compare(this, another);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o != null && o.getClass() == this.getClass()) {
            FastTime another = (FastTime) o;
            return another.s3 == this.s3;
        }
        return false;
    }

    public long getMilliseconds() {
        return s3;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(s3);
    }

    @Override
    public String toString() {
        return toDateString();
    }

    public String toDateString() {
        long seconds = s3 / MILLISECONDS_PER_SECOND;
        int millisecondOfSecond = (int) (s3 % MILLISECONDS_PER_SECOND);
        return String.format("%ld.%03d", seconds, millisecondOfSecond);
    }

    public String toSpanString() {
        long seconds = s3 / MILLISECONDS_PER_SECOND;
        int millisecondOfSecond = (int) (s3 % MILLISECONDS_PER_SECOND);
        return String.format("P%ld.%03d", seconds, millisecondOfSecond);
    }
}
