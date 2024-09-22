package pd.util;

import java.util.Objects;

public class TextNumber extends Number {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected String numberString;

    public TextNumber() {
        this(0);
    }

    public TextNumber(double value) {
        this(Double.toString(value));
    }

    public TextNumber(long value) {
        this(Long.toString(value));
    }

    public TextNumber(String s) {
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
        if (o instanceof TextNumber) {
            TextNumber another = (TextNumber) o;
            return Objects.equals(another.numberString, numberString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(numberString);
    }

    @Override
    public float floatValue() {
        return Float.parseFloat(numberString);
    }

    @Override
    public double doubleValue() {
        return Double.parseDouble(numberString);
    }

    @Override
    public int intValue() {
        return Integer.parseInt(numberString);
    }

    @Override
    public long longValue() {
        return Long.parseLong(numberString);
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
        double f = getFloat64();
        return f == (long) f;
    }

    public TextNumber set(double value) {
        numberString = Double.toString(value);
        return this;
    }

    public TextNumber set(long value) {
        numberString = Long.toString(value);
        return this;
    }

    public TextNumber set(String s) {
        if (s == null) {
            throw new NullPointerException();
        }
        double ignored = Double.parseDouble(s);
        numberString = s;
        return this;
    }

    @Override
    public String toString() {
        return numberString;
    }
}
