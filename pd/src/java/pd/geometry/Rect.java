package pd.geometry;

/**
 * immutable bound box, 2-dimensional rectangular coordinate system<br/>
 * ranges from [x0,x1) x [y0,y1)
 */
public final class Rect {

    public final int x0, y0;
    public final int x1, y1;

    public Rect(int x0, int y0, int x1, int y1) {
        assert x0 <= x1;
        assert y0 <= y1;

        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public boolean contains(int x, int y) {
        return x >= x0 && x < x1 && y >= y0 && y < y1;
    }

    public boolean contains(Rect r) {
        return this.x0 <= r.x0
                && this.y0 <= r.y0
                && this.x1 >= r.x1
                && this.y1 >= r.y1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o instanceof Rect) {
            Rect r = (Rect) o;
            return this.x0 == r.x0
                    && this.y0 == r.y0
                    && this.x1 == r.x1
                    && this.y1 == r.y1;
        }
        return false;
    }

    public int getHeight() {
        return y1 - y0;
    }

    public Rect getIntersection(Rect r) {
        assert r != null;

        int x0 = Math.max(this.x0, r.x0);
        int y0 = Math.max(this.y0, r.y0);
        int x1 = Math.min(this.x1, r.x1);
        int y1 = Math.min(this.y1, r.y1);

        if (x0 <= x1 && y0 <= y1) {
            return new Rect(x0, y0, x1, y1);
        } else {
            return null;
        }
    }

    public int getWidth() {
        return x1 - x0;
    }

    @Override
    public int hashCode() {
        int hashCode = x0;
        hashCode = 31 * hashCode + y0;
        hashCode = 31 * hashCode + x1;
        hashCode = 31 * hashCode + y1;
        return hashCode;
    }

    public Rect inset(int dx, int dy) {
        return new Rect(x0 + dx, y0 + dy, x1 - dx, y1 - dy);
    }

    /**
     * return <code>true</code> if the 2 rects have at least 1 mutual point
     */
    public boolean intersects(Rect box) {
        assert box != null;

        if (this == box) {
            return true;
        }
        return getIntersection(box) != null;
    }

    public Rect move(int dx, int dy) {
        return new Rect(x0 + dx, y0 + dy, x1 + dx, y1 + dy);
    }

    /**
     * implies the anchor<br/>
     */
    public Rect moveTo(int x0, int y0) {
        return new Rect(x0, y0, x0 + getWidth(), y0 + getHeight());
    }
}
