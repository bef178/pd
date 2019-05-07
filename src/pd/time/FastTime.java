package pd.time;

import java.io.Serializable;

/**
 * Represents a time point, started from Unix Epoch (1970-01-01 00:00 +0000).
 * After {@link TimeSpan}.
 */
public final class FastTime implements Comparable<FastTime>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

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

    public static FastTime now() {
        return new FastTime(System.currentTimeMillis());
    }

    /**
     * in milliseconds
     */
    private final long s3;

    public FastTime(long offsetMilliseconds) {
        this.s3 = offsetMilliseconds;
    }

    @Override
    public int compareTo(FastTime o) {
        return FastTime.compare(this, o);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj != null && obj.getClass() == this.getClass()) {
            return ((FastTime) obj).s3 == this.s3;
        }
        return false;
    }

    /**
     * milliseconds since Unix Epoch
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
        double seconds = 1.0d * s3 / TimeSpan.MILLISECONDS_PER_SECOND;
        return String.format("%.03f", seconds);
    }
}
