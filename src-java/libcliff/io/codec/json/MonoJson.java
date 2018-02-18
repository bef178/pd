package libcliff.io.codec.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

class MonoJson implements JsonScalar, JsonVector, JsonObject {

    private String s;

    private ArrayList<Json> l;

    private HashMap<String, Json> m;

    private final JsonType type;

    MonoJson(JsonType type) {
        this.type = type;
        switch (type) {
            case SCALAR:
                break;
            case VECTOR:
                l = new ArrayList<>();
                break;
            case OBJECT:
                m = new HashMap<>();
                break;
            default:
                throw new IllegalJsonTypeException();
        }
    }

    private Json checkType(JsonType expected) {
        return Json.checkType(this, expected);
    };

    @Override
    public MonoJson clear() {
        if (s != null) {
            s = null;
        }
        if (l != null) {
            l.clear();
        }
        if (m != null) {
            m.clear();
        }
        return this;
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
    public Json getJson(int index) {
        checkType(JsonType.VECTOR);
        return l.get(index);
    }

    private Json getJson(int index, JsonType expected) {
        return Json.checkType(getJson(index), expected);
    }

    @Override
    public Json getJson(String key) {
        checkType(JsonType.OBJECT);
        return m.get(key);
    }

    private Json getJson(String key, JsonType valueType) {
        return Json.checkType(getJson(key), valueType);
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return (JsonObject) getJson(index, JsonType.OBJECT);
    }

    @Override
    public JsonObject getJsonObject(String key) {
        return (JsonObject) getJson(key, JsonType.OBJECT);
    }

    @Override
    public JsonScalar getJsonScalar(int index) {
        return (JsonScalar) getJson(index, JsonType.SCALAR);
    }

    @Override
    public JsonScalar getJsonScalar(String key) {
        return (JsonScalar) getJson(key, JsonType.SCALAR);
    }

    @Override
    public JsonVector getJsonVector(int index) {
        return (JsonVector) getJson(index, JsonType.VECTOR);
    }

    @Override
    public JsonVector getJsonVector(String key) {
        return (JsonVector) getJson(key, JsonType.VECTOR);
    }

    @Override
    public String getString() {
        checkType(JsonType.SCALAR);
        return s;
    }

    @Override
    public JsonVector insert(int index, Json value) {
        checkType(JsonType.VECTOR);
        l.add(index, value);
        return this;
    }

    @Override
    public JsonVector insert(Json value) {
        checkType(JsonType.VECTOR);
        l.add(value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Set<String> keys() {
        checkType(JsonType.OBJECT);
        return m.keySet();
    }

    @Override
    public JsonObject put(String key, Json value) {
        checkType(JsonType.OBJECT);
        m.put(key, value);
        return this;
    }

    @Override
    public JsonVector remove(int index) {
        checkType(JsonType.VECTOR);
        l.remove(index);
        return this;
    }

    @Override
    public MonoJson remove(String key) {
        checkType(JsonType.OBJECT);
        m.remove(key);
        return this;
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
    public JsonVector set(int index, Json value) {
        checkType(JsonType.VECTOR);
        l.set(index, value);
        return this;
    }

    @Override
    public JsonScalar set(Integer value) {
        return set(value.toString());
    }

    @Override
    public JsonScalar set(String s) {
        checkType(JsonType.SCALAR);
        this.s = s;
        return this;
    }

    @Override
    public int size() {
        switch (type()) {
            case VECTOR:
                return l.size();
            case OBJECT:
                return m.size();
            default:
                throw new IllegalJsonTypeException();
        }
    }

    @Override
    public JsonType type() {
        return this.type;
    }
}
