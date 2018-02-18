package libcliff.io.codec.json;

import java.util.HashMap;
import java.util.Set;

class SimpleJsonObject implements JsonObject {

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

    private Json getJson(String key, JsonType valueType) {
        return Json.checkType(m.get(key), valueType);
    }

    @Override
    public JsonObject getJsonObject(String key) {
        return (JsonObject) getJson(key, JsonType.OBJECT);
    }

    @Override
    public JsonScalar getJsonScalar(String key) {
        return (JsonScalar) getJson(key, JsonType.SCALAR);
    }

    @Override
    public JsonVector getJsonVector(String key) {
        return (JsonVector) getJson(key, JsonType.VECTOR);
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
    public int size() {
        return m.size();
    }

    @Override
    public JsonType type() {
        return JsonType.OBJECT;
    }
}
