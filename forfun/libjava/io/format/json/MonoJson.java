package libjava.io.format.json;

import static libjava.io.format.json.SimpleJsonObject.checkType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import libjava.io.Pushable;
import libjava.io.format.json.JsonFactory.Config;
import libjava.io.format.json.JsonFactory.JsonSerializer;

class MonoJson implements JsonScalar, JsonVector, JsonObject, Json {

    static enum JsonType {
        SCALAR, VECTOR, OBJECT;
    }

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

    private void checkOwnType(JsonType type) {
        if (this.type != type) {
            throw new IllegalJsonTypeException();
        }
    }

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
        checkOwnType(JsonType.VECTOR);
        return l.get(index);
    }

    @Override
    public Json getJson(String key) {
        checkOwnType(JsonType.OBJECT);
        return m.get(key);
    }

    @Override
    public JsonObject getJsonObject(int index) {
        assert this.type == JsonType.VECTOR;
        return checkType(getJson(index), JsonObject.class);
    }

    @Override
    public JsonObject getJsonObject(String key) {
        return checkType(getJson(key), JsonObject.class);
    }

    @Override
    public JsonScalar getJsonScalar(int index) {
        return checkType(getJson(index), JsonScalar.class);
    }

    @Override
    public JsonScalar getJsonScalar(String key) {
        return checkType(getJson(key), JsonScalar.class);
    }

    @Override
    public JsonVector getJsonVector(int index) {
        return checkType(getJson(index), JsonVector.class);
    }

    @Override
    public JsonVector getJsonVector(String key) {
        return checkType(getJson(key), JsonVector.class);
    }

    @Override
    public String getString() {
        checkOwnType(JsonType.SCALAR);
        return s;
    }

    @Override
    public JsonVector insert(int index, Json value) {
        checkOwnType(JsonType.VECTOR);
        l.add(index, value);
        return this;
    }

    @Override
    public JsonVector insert(Json value) {
        checkOwnType(JsonType.VECTOR);
        l.add(value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Set<String> keys() {
        checkOwnType(JsonType.OBJECT);
        return m.keySet();
    }

    @Override
    public JsonObject put(String key, Json value) {
        checkOwnType(JsonType.OBJECT);
        m.put(key, value);
        return this;
    }

    @Override
    public JsonVector remove(int index) {
        checkOwnType(JsonType.VECTOR);
        l.remove(index);
        return this;
    }

    @Override
    public MonoJson remove(String key) {
        checkOwnType(JsonType.OBJECT);
        m.remove(key);
        return this;
    }

    @Override
    public void serialize(Config config, Pushable it) {
        switch (type) {
            case SCALAR:
                JsonSerializer.serializeScalar(this, it);
                break;
            case VECTOR:
                JsonSerializer.serializeVector(this, config, it);
                break;
            case OBJECT:
                JsonSerializer.serializeObject(this, config, it);
                break;
            default:
                throw new IllegalJsonTypeException();
        }
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
        checkOwnType(JsonType.VECTOR);
        l.set(index, value);
        return this;
    }

    @Override
    public JsonScalar set(Integer value) {
        return set(value.toString());
    }

    @Override
    public JsonScalar set(String s) {
        checkOwnType(JsonType.SCALAR);
        this.s = s;
        return this;
    }

    @Override
    public int size() {
        switch (type) {
            case VECTOR:
                return l.size();
            case OBJECT:
                return m.size();
            default:
                throw new IllegalJsonTypeException();
        }
    }
}
