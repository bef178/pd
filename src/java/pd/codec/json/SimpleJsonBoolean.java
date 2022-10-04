package pd.codec.json;

class SimpleJsonBoolean implements IJsonBoolean {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private boolean value;

    public SimpleJsonBoolean() {
        this(false);
    }

    private SimpleJsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public boolean getBoolean() {
        return value;
    }

    @Override
    public SimpleJsonBoolean set(boolean value) {
        this.value = value;
        return this;
    }
}
