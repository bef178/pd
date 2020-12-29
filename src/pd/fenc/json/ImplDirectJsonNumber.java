package pd.fenc.json;

import pd.fenc.NumberToken;

class ImplDirectJsonNumber extends NumberToken implements IJsonNumber {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    ImplDirectJsonNumber(Number value) {
        set(value.toString());
    }

    @Override
    public String serialize() {
        return toString();
    }

    @Override
    public float valueToFloat32() {
        return floatValue();
    }

    @Override
    public double valueToFoat64() {
        return doubleValue();
    }

    @Override
    public int valueToInt32() {
        return intValue();
    }

    @Override
    public long valueToInt64() {
        return longValue();
    }
}
