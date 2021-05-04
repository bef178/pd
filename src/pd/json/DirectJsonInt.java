package pd.json;

class DirectJsonInt implements IJsonInt {

    private long value;

    public DirectJsonInt() {
        this(0);
    }

    public DirectJsonInt(long value) {
        set(value);
    }

    @Override
    public void set(long value) {
        this.value = value;

    }

    @Override
    public int int32() {
        return (int) value;
    }

    @Override
    public long int64() {
        return value;
    }
}
