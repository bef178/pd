package t.typedef.geometry;

public final class Point {

	public final int x;
	public final int y;

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Point) {
			Point point = (Point) o;
			return this.x == point.x && this.y == point.y;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return x * 31 + y;
	}

	@Override
	public String toString() {
		return "(" + this.x + ',' + this.y + ')';
	}
}
