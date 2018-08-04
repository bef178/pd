package libjava.geography;

/**
 * immutable point on the surface of a sphere<br/>
 * using double elements as lat, lon<br/>
 *
 * @author tanghao
 */
public class Point {

    public static boolean isValidLat(double lat) {
        return lat >= -90.0 && lat <= 90.0;
    }

    public static boolean isValidLon(double lon) {
        return lon >= -180.0 && lon <= 180.0;
    }

    public final double lat;
    public final double lon;

    public Point(double lat, double lon) {
        if (!isValidLat(lat)) {
            throw new IllegalArgumentException("INVALID DOUBLE LAT");
        }
        if (!isValidLon(lon)) {
            throw new IllegalArgumentException("INVALID DOUBLE LON");
        }
        this.lat = lat;
        this.lon = lon;
    }

    public boolean isValid() {
        return Point.isValidLat(lat) && Point.isValidLon(lon);
    }

    public String toStandardString() {
        StringBuilder sb = new StringBuilder();

        if (lat >= 0) {
            sb.append(String.format("%.5fN", lat));
        } else {
            sb.append(String.format("%.5fS", -lat));
        }
        sb.append(":");
        if (lon >= 0) {
            sb.append(String.format("%.5fE", lon));
        } else {
            sb.append(String.format("%.5fW", -lon));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toString(true, ":");
    }

    public String toString(boolean withPrefix, String separator) {
        StringBuilder sb = new StringBuilder();
        if (withPrefix) {
            sb.append("LAT=").append(lat).append(separator);
            sb.append("LON=").append(lon);
        } else {
            sb.append(lat).append(separator);
            sb.append(lon);
        }
        return sb.toString();
    }
}
