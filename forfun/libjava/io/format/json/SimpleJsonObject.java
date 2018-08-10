package libjava.io.format.json;

import java.util.HashMap;
import java.util.Set;

import libjava.io.Pushable;
import libjava.io.format.json.JsonFactory.Config;
import libjava.io.format.json.JsonFactory.JsonSerializer;

class SimpleJsonObject implements JsonObject {

    public static <T extends Json> T checkType(Json json, Class<T> expected) {
        if (json == null || !expected.isInstance(json)) {
            throw new IllegalJsonTypeException();
        }
        return expected.cast(json);
    }

    private HashMap<String, Json> m = new HashMap<>();

    SimpleJsonObject() {
        // dummy
    }

    @Override
    public JsonObject clear() {
        m.clear();
        return this;
    }

    @Override
    public Json getJson(String key) {
        return m.get(key);
    }

    @Override
    public JsonObject getJsonObject(String key) {
        return checkType(m.get(key), JsonObject.class);
    }

    @Override
    public JsonScalar getJsonScalar(String key) {
        return checkType(m.get(key), JsonScalar.class);
    }

    @Override
    public JsonVector getJsonVector(String key) {
        return checkType(m.get(key), JsonVector.class);
    }

    @Override
    public boolean isEmpty() {
        return m.isEmpty();
    }

    @Override
    public Set<String> keys() {
        return m.keySet();
    }

    @Override
    public JsonObject put(String key, Json value) {
        m.put(key, value);
        return this;
    }

    @Override
    public JsonObject remove(String key) {
        m.remove(key);
        return this;
    }

    @Override
    public void serialize(Config config, Pushable it) {
        JsonSerializer.serializeObject(this, config, it);
    }

    @Override
    public int size() {
        return m.size();
    }
}
