package t.typedef.json.mono;

import t.typedef.json.Json;
import t.typedef.json.JsonMapping;
import t.typedef.json.JsonScalar;
import t.typedef.json.JsonSequence;

abstract class AbsMonoJson implements JsonScalar, JsonSequence,
        JsonMapping {

    private final Type type;

    AbsMonoJson(Type type) {
        this.type = type;
    }

    protected void checkInstance(Json j) {
        if (j instanceof AbsMonoJson) {
            return;
        }
        throw new IllegalTypeException();
    }

    /**
     * assert type equals or throw {@link IllegalTypeException}
     */
    protected final void checkType(Type expected) {
        if (this.type() != expected) {
            throw new IllegalTypeException();
        }
    }

    @Override
    public abstract AbsMonoJson clear();

    @Override
    public Boolean getBoolean() {
        return Boolean.valueOf(getString());
    };

    @Override
    public Double getDouble() {
        return Double.valueOf(getString());
    }

    @Override
    public Integer getInteger() {
        return Integer.valueOf(getString());
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
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
    public final Type type() {
        return this.type;
    }
}
