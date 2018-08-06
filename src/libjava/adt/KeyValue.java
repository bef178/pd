package libjava.adt;

import libjava.primitive.Cstring;

public class KeyValue {

    public static KeyValue fromString(String s, int separator) {
        String[] pair = Cstring.split(s, separator);
        switch (pair.length) {
            case 1:
                return new KeyValue(pair[0], "");
            case 2:
                return new KeyValue(pair[0], pair[1]);
            default:
                break;
        }
        throw new IllegalArgumentException();
    }

    public static KeyValue[] fromString(String s, int spMajor, int spMinor) {
        String[] a = Cstring.split(s, spMajor);
        KeyValue[] items = new KeyValue[a.length];
        for (int i = 0; i < a.length; ++i) {
            items[i] = KeyValue.fromString(a[i], spMinor);
        }
        return items;
    }

    protected final String key;

    protected final String value;

    public KeyValue(String key, String value) {
        assert key != null;
        this.key = key;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (this.getClass() != o.getClass()) {
            return false;
        }
        KeyValue a = (KeyValue) o;
        return Cstring.equals(this.key, a.key) && Cstring.equals(this.value, a.value);
    }

    @Override
    public int hashCode() {
        return Cstring.hashCode(this.key, this.value);
    }

    @Override
    public String toString() {
        return toString('=');
    }

    public String toString(int delimeter) {
        return new StringBuilder()
                .append(key).appendCodePoint(delimeter).append(value)
                .toString();
    }
}
