package pd.json.simplejson;

import pd.json.type.IJsonString;

class SimpleJsonString implements IJsonString {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private String value = null;

    public SimpleJsonString() {
        this("");
    }

    public SimpleJsonString(String value) {
        set(value);
    }

    @Override
    public String getString() {
        return value;
    }

    @Override
    public SimpleJsonString set(String value) {
        this.value = value;
        return this;
    }
}
