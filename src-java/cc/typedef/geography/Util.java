package cc.typedef.geography;

public final class Util {

    // average Earth radius, from wikipedia
    private static final int EARTH_RADIUS = 6371009;

    /**
     * find the balance of given degrees, values in (-180, 180]<br/>
     */
    public static double diffDegrees(double degrees1, double degrees2) {
        assert isNormalizedDegrees(degrees1);
        assert isNormalizedDegrees(degrees2);

        double balance = degrees2 - degrees1;
        if (balance <= -180) {
            balance += 360;
        } else if (balance > 180) {
            balance -= 360;
        }
        return balance;
    }

    /**
     * find the absolute balance of given degrees, values in [0, 180]<br/>
     */
    public static double diffDegreesAbs(double degrees1, double degrees2) {
        assert isNormalizedDegrees(degrees1);
        assert isNormalizedDegrees(degrees2);

        return Math.abs(diffDegrees(degrees1, degrees2));
    }

    /**
     * find the angle from North, clockwise, values in degrees, [0, 360)<br/>
     * using rectangular coordinates instead of spherical<br/>
     * (assuming close enough)<br/>
     */
    public static double findAzimuthDegrees(Point s, Point t) {
        assert s != null;
        assert s.isValid();
        assert t != null;
        assert t.isValid();

        final int MAGNIFICATION = 100000;

        double dy = MAGNIFICATION * (t.lat - s.lat);

        double dx = MAGNIFICATION * (t.lon - s.lon)
            * Math.cos(Math.toRadians((s.lat + t.lat) * 0.5));

        double radians = Math.atan2(dy, dx);

        radians = Math.PI * 0.5 - radians;
        if (radians < 0) {
            radians += Math.PI * 2;
        }
        return Math.toDegrees(radians);
    }

    // 应计算弧sN在s的切射线与弧st在s的切射线的夹角，即截面OsN与截面Ost的夹角
    // h1: the heading of the tangent line of arc sN at point s
    // h2: the heading of the tangent line of arc st at point s
    // should calculate the angle between h1 and h2,
    // i.e., the angle between flat OsN and flat Ost
    @Deprecated
    public static double findAzimuthDegreesAccurate(Point s, Point t) {
        assert s != null && s.isValid();
        assert t != null && t.isValid();

        throw new Error("unimplemented yet");
    }

    private static double findDeflectionRadians(double rLat1, double rLon1,
        double rLat2, double rLon2) {
        // apply the formula
        double rad = Math.sin(rLat1) * Math.sin(rLat2);
        rad += Math.cos(rLat1) * Math.cos(rLat2) * Math.cos(rLon1 - rLon2);
        if (rad > 1.0 || rad < 0.0) {
            rad = Math.round(rad);
        }
        assert rad <= 1.0 && rad >= 0.0;
        return Math.acos(rad);
    }

    private static double findDeflectionRadiansAccurate(double rLat1,
        double rLon1, double rLat2, double rLon2) {
        // another formula said to be more accurate
        double rad = square_sin_half(rLat1 - rLat2);
        rad += Math.cos(rLat1) * Math.cos(rLat2)
            * square_sin_half(rLon1 - rLon2);
        rad = Math.sqrt(rad);
        return Math.asin(rad) * 2;
    }

    public static double findDistanceOnTheEarth(Point s, Point t) {
        assert s != null;
        assert s.isValid();
        assert t != null;
        assert t.isValid();

        double rLat1 = Math.toRadians(s.lat);
        double rLon1 = Math.toRadians(s.lon);

        double rLat2 = Math.toRadians(t.lat);
        double rLon2 = Math.toRadians(t.lon);

        boolean accurate = false;

        if (accurate) {
            return EARTH_RADIUS
                * findDeflectionRadiansAccurate(rLat1, rLon1, rLat2, rLon2);
        } else {
            return EARTH_RADIUS
                * findDeflectionRadians(rLat1, rLon1, rLat2, rLon2);
        }
    }

    private static final boolean isNormalizedDegrees(double degrees) {
        return degrees >= 0.0 && degrees < 360.0;
    }

    private static final double square_sin_half(double rAngle) {
        return Math.pow(Math.sin(rAngle * 0.5), 2.0);
    }

    private Util() {
        throw new Error("No instance allowed");
    }
}
