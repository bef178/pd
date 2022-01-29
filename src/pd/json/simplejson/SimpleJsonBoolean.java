package pd.json.simplejson;

import pd.json.type.IJsonBoolean;

class SimpleJsonBoolean implements IJsonBoolean {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public static final SimpleJsonBoolean jsonFalse = new SimpleJsonBoolean(false);

    public static final SimpleJsonBoolean jsonTrue = new SimpleJsonBoolean(true);

    private final boolean value;

    private SimpleJsonBoolean(boolean value) {
        this.value = value;
    }

    @Override
    public boolean getBoolean() {
        return value;
    }
}
