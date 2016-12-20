package cc.typedef.geometry;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Polygon {

    public static boolean isStraight(Point p0, Point p1, Point p2) {
        int dx01 = p1.x - p0.x;
        int dy01 = p1.y - p0.y;
        int dx12 = p2.x - p1.x;
        int dy12 = p2.y - p1.y;
        return dy12 * dx01 == dy01 * dx12;
    }

    public static void main(String[] args) {
        List<Point> l = new LinkedList<Point>();
        l.add(new Point(1, 1));
        l.add(new Point(2, 2));
        l.add(new Point(5, 1));
        l.add(new Point(3, 1));
        l.add(new Point(3, 0));
        Polygon p = new Polygon();
        p.fromSegments(l);
        System.out.println(p.area());

        l.clear();
        l.add(new Point(1, 2));
        l.add(new Point(3, 1));
        l.add(new Point(0, 0));
        p.fromSegments(l);
        System.out.println(p.area());
    }

    /**
     * Note: Formally, the polygon has direction. That is, when we walk along
     * the edge, the polygon is on our left side. If it is the case, we get a
     * positive area.<br/>
     */
    private LinkedList<Point> points = new LinkedList<Point>();

    public double area() {
        if (!isValid()) {
            throw new IllegalStateException();
        }

        Point pivot;
        {
            Point p = points.get(points.size() / 4);
            Point q = points.get(points.size() / 4 * 3);
            pivot = new Point(p.x + (q.x - p.x) / 2, p.y + (q.y - p.y) / 2);
        }

        int area = 0;
        Iterator<Point> it = points.iterator();
        Point p0 = it.next();
        while (it.hasNext()) {
            Point p1 = it.next();
            area += (p0.x - pivot.x) * (p1.y - pivot.y)
                    - (p1.x - pivot.x) * (p0.y - pivot.y);
            p0 = p1;
        }
        return area / 2.0;
    }

    public boolean fromSegments(List<Point> points) {
        if (points == null || points.size() < 3) {
            return false;
        }

        LinkedList<Point> myPoints = new LinkedList<Point>();

        Iterator<Point> it = points.iterator();

        Point p0 = it.next();

        Point p1 = it.next();
        while (p1.equals(p0) && it.hasNext()) {
            p1 = it.next();
        }
        if (p1.equals(p0)) {
            return false;
        }

        while (it.hasNext()) {
            Point p2 = it.next();
            if (p2.equals(p1)) {
                continue;
            }

            if (isStraight(p0, p1, p2)) {
                p1 = p2;
                continue;
            }

            // TODO return false if the curve touches itself

            myPoints.add(p0);
            p0 = p1;
            p1 = p2;
        }
        myPoints.add(p0);
        if (!p1.equals(myPoints.get(0))) {
            myPoints.add(p1);
            myPoints.add(myPoints.get(0));
        }

        if (myPoints.size() < 4) {
            return false;
        }

        this.points = myPoints;
        return true;
    }

    public boolean isValid() {
        return points != null && points.size() > 3;
    }
}
