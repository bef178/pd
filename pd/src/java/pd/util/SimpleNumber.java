package pd.util;

import java.util.Objects;

public class SimpleNumber extends Number {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected Number numberValue;

    public SimpleNumber() {
        this(0);
    }

    public SimpleNumber(double value) {
        this(Double.toString(value));
    }

    public SimpleNumber(long value) {
        this(Long.toString(value));
    }

    public SimpleNumber(String s) {
        set(s);
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof SimpleNumber) {
            SimpleNumber another = (SimpleNumber) o;
            return Objects.equals(another.numberValue, numberValue);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numberValue);
    }

    @Override
    public float floatValue() {
        return numberValue.floatValue();
    }

    @Override
    public double doubleValue() {
        return numberValue.doubleValue();
    }

    @Override
    public int intValue() {
        return numberValue.intValue();
    }

    @Override
    public long longValue() {
        return numberValue.longValue();
    }

    public float getFloat32() {
        return floatValue();
    }

    public double getFloat64() {
        return doubleValue();
    }

    public int getInt32() {
        return intValue();
    }

    public long getInt64() {
        return longValue();
    }

    public boolean isRoundNumber() {
        return numberValue instanceof Long;
    }

    public SimpleNumber set(double value) {
        numberValue = value;
        return this;
    }

    public SimpleNumber set(long value) {
        numberValue = value;
        return this;
    }

    public SimpleNumber set(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        if (s.indexOf('.') < 0) {
            numberValue = Long.parseLong(s);
        } else {
            numberValue = Double.parseDouble(s);
        }
        return this;
    }

    @Override
    public String toString() {
        return numberValue.toString();
    }
}
