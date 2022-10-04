package pd.codec.json;

import pd.fenc.TextNumber;

public class SimpleJsonNumber extends TextNumber implements IJsonNumber {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SimpleJsonNumber() {
        this(0);
    }

    public SimpleJsonNumber(double value) {
        super(value);
    }

    public SimpleJsonNumber(long value) {
        super(value);
    }

    @Override
    public double getFloat64() {
        return doubleValue();
    }

    @Override
    public long getInt64() {
        return longValue();
    }

    @Override
    public SimpleJsonNumber set(double value) {
        super.set(value);
        return this;
    }

    @Override
    public SimpleJsonNumber set(long value) {
        super.set(value);
        return this;
    }

    @Override
    public SimpleJsonNumber set(String raw) {
        super.set(raw);
        return this;
    }
}
