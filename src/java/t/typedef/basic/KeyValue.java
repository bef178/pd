package t.typedef.basic;

public class KeyValue {

    public static final boolean eq(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    protected String k = null;

    protected String v = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (getClass() == o.getClass()) {
            KeyValue entry = (KeyValue) o;
            return eq(this.k, entry.k)
                    && eq(this.v, entry.v);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 0;
        if (k != null) {
            h = k.hashCode();
        }
        if (v != null) {
            h = 31 * h + v.hashCode();
        }
        return h;
    }

    public boolean isValid() {
        return k != null && !k.isEmpty();
    }
}
