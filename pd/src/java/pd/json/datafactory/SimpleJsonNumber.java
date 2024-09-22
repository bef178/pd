package pd.json.datafactory;

import pd.json.datatype.JsonNumber;
import pd.util.TextNumber;

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
    public SimpleJsonNumber set(String s) {
        super.set(s);
        return this;
    }
}
