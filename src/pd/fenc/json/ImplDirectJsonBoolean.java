package pd.fenc.json;

class ImplDirectJsonBoolean implements IJsonBoolean {

    private boolean value;

    ImplDirectJsonBoolean() {
        this(false);
    }

    ImplDirectJsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public String serialize() {
        return Boolean.toString(value);
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
