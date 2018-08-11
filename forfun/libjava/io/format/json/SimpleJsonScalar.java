package libjava.io.format.json;

import libjava.io.Pushable;

class SimpleJsonScalar implements JsonScalar {

    private String s = null;

    SimpleJsonScalar() {
        // dummy
    }

    @Override
    public Boolean getBoolean() {
        return Boolean.valueOf(getString());
    }

    @Override
    public Double getDouble() {
        return Double.valueOf(getString());
    }

    @Override
    public Integer getInteger() {
        return Integer.valueOf(getString());
    }

    @Override
    public String getString() {
        return s;
    }

    @Override
    public void serialize(FormattingConfig ignored, Pushable it) {
        JsonSerializer.serializeScalar(this, it);
    }

    @Override
    public JsonScalar set(Boolean value) {
        return set(value.toString());
    }

    @Override
    public JsonScalar set(Double value) {
        return set(value.toString());
    }

    @Override
    public JsonScalar set(Integer value) {
        return set(value.toString());
    }

    @Override
    public JsonScalar set(String s) {
        this.s = s;
        return this;
    }
}
