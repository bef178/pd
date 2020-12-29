package pd.fenc;

public class NumberToken extends Number {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String raw;

    public NumberToken() {
        this(0);
    }

    public NumberToken(double value) {
        this(Double.toString(value));
    }

    public NumberToken(long value) {
        this(Long.toString(value));
    }

    public NumberToken(String raw) {
        set(raw);
    }

    @Override
    public double doubleValue() {
        return Double.parseDouble(raw);
    }

    @Override
    public float floatValue() {
        return Float.parseFloat(raw);
    }

    @Override
    public int intValue() {
        return Integer.parseInt(raw);
    }

    @Override
    public long longValue() {
        return Long.parseLong(raw);
    }

    public void set(double value) {
        raw = Double.toString(value);
    }

    public void set(long value) {
        raw = Long.toString(value);
    }

    public void set(String raw) {
        if (raw == null) {
            throw new NullPointerException();
        }
        try {
            Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw new ParsingException(String.format("unrecognized number [%s]", raw));
        }
        this.raw = raw;
    }

    @Override
    public String toString() {
        return raw;
    }
}
