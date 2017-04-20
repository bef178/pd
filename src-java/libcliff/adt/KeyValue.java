package libcliff.adt;

public class KeyValue {

    public static final boolean eq(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    protected String key = null;

    protected String value = null;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (getClass() == o.getClass()) {
            KeyValue entry = (KeyValue) o;
            return eq(this.key, entry.key)
                    && eq(this.value, entry.value);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 0;
        if (key != null) {
            h = key.hashCode();
        }
        if (value != null) {
            h = 31 * h + value.hashCode();
        }
        return h;
    }

    public boolean isValid() {
        return key != null && !key.isEmpty();
    }
}
