package pd.json;

class DirectJsonBoolean implements IJsonBoolean {

    private boolean value;

    public DirectJsonBoolean() {
        this(false);
    }

    public DirectJsonBoolean(boolean value) {
        set(value);
    }

    @Override
    public void set(boolean value) {
        this.value = value;
    }

    @Override
    public boolean value() {
        return value;
    }
}
