package cc.typedef.json.mono;

import cc.typedef.json.IllegalTypeException;
import cc.typedef.json.Json;
import cc.typedef.json.JsonDict;
import cc.typedef.json.JsonList;
import cc.typedef.json.JsonScalar;

abstract class AbsMonoJson implements JsonScalar, JsonList, JsonDict {

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
