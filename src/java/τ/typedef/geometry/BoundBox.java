package τ.typedef.geometry;

/**
 * immutable bound box, 2-dimensional rectangular coordinate system<br/>
 */
public final class BoundBox {

	public final int minX;
	public final int minY;
	public final int maxX;
	public final int maxY;

	public BoundBox(int minX, int minY, int maxX, int maxY) {
		assert minX <= maxX;
		assert minY <= maxY;

		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}

	public boolean contains(BoundBox box) {
		return this.minX <= box.minX
				&& this.minY <= box.minY
				&& this.maxX >= box.maxX
				&& this.maxY >= box.maxY;
	}

	public boolean contains(int x, int y) {
		return x >= minX && x <= maxX && y >= minY && y <= maxY;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o instanceof BoundBox) {
			BoundBox box = (BoundBox) o;
			return this.minX == box.minX
					&& this.minY == box.minY
					&& this.maxX == box.maxX
					&& this.maxY == box.maxY;
		}
		return false;
	}

	public BoundBox findIntersection(BoundBox box) {
		assert box != null : "@box: null";

		int minX = Math.max(this.minX, box.minX);
		int minY = Math.max(this.minY, box.minY);
		int maxX = Math.min(this.maxX, box.maxX);
		int maxY = Math.min(this.maxY, box.maxY);

		if (minX <= maxX && minY <= maxY) {
			return new BoundBox(minX, minY, maxX, maxY);
		} else {
			return null;
		}
	}

	@Override
	public int hashCode() {
		int hashCode = minX;
		hashCode = 31 * hashCode + minY;
		hashCode = 31 * hashCode + maxX;
		hashCode = 31 * hashCode + maxY;
		return hashCode;
	}

	public int height() {
		return maxY - minY + 1;
	}

	public BoundBox inset(int dx, int dy) {
		return new BoundBox(minX + dx, minY + dy, maxX - dx, maxY - dy);
	}

	/**
	 * @return <code>true</code> if the 2 boxes have at least 1 mutual point
	 */
	public boolean intersects(BoundBox box) {
		assert box != null : "@box: null";

		if (this == box) {
			return true;
		}
		return findIntersection(box) != null;
	}

	public BoundBox move(int dx, int dy) {
		return new BoundBox(minX + dx, minY + dy, maxX + dx, maxY + dy);
	}

	public BoundBox moveTo(int minX, int minY) {
		return new BoundBox(minX, minY, minX + width(), minY + height());
	}

	public int width() {
		return maxX - minX + 1;
	}
}
