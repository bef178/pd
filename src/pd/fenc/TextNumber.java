package pd.fenc;

public class TextNumber extends Number {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    protected String raw;

    public TextNumber() {
        this(0);
    }

    public TextNumber(double value) {
        this(Double.toString(value));
    }

    public TextNumber(long value) {
        this(Long.toString(value));
    }

    public TextNumber(String raw) {
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

    public TextNumber set(double value) {
        raw = Double.toString(value);
        return this;
    }

    public TextNumber set(long value) {
        raw = Long.toString(value);
        return this;
    }

    public TextNumber set(String raw) {
        if (raw == null) {
            throw new NullPointerException();
        }
        try {
            Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            throw new ParsingException(String.format("unrecognized number [%s]", raw));
        }
        this.raw = raw;
        return this;
    }

    @Override
    public String toString() {
        return raw;
    }
}
