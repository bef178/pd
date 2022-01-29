package pd.json.simplejson;

import pd.fenc.TextNumber;
import pd.json.type.IJsonNumber;

public class SimpleJsonNumber extends TextNumber implements IJsonNumber {

    /**
    *
    */
    private static final long serialVersionUID = 1L;

    public SimpleJsonNumber() {
        super();
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
