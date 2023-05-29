package pd.codec.json.datafactory;

import pd.codec.json.datatype.JsonNumber;
import pd.fenc.TextNumber;

final class SimpleJsonNumber extends TextNumber implements JsonNumber {

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
    public boolean isRoundNumber() {
        double f = getFloat64();
        return f == (long) f;
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
