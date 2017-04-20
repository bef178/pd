package libcliff.json.simple;

import java.util.HashMap;
import java.util.Set;

import libcliff.json.IllegalTypeException;
import libcliff.json.Json;
import libcliff.json.JsonDict;
import libcliff.json.JsonList;
import libcliff.json.JsonScalar;

class SimpleJsonDict implements JsonDict {

    private HashMap<String, Json> m = new HashMap<>();

    SimpleJsonDict() {
        // dummy
    }

    @Override
    public JsonDict clear() {
        m.clear();
        return this;
    }

    @Override
    public Json getJson(String key) {
        return m.get(key);
    }

    private Json getJson(String key, Type t) {
        Json j = m.get(key);
        if (j != null && j.type() == t) {
            return j;
        }
        throw new IllegalTypeException();
    }

    @Override
    public JsonDict getJsonDict(String key) {
        return (JsonDict) getJson(key, Type.DICT);
    }

    @Override
    public JsonScalar getJsonScalar(String key) {
        return (JsonScalar) getJson(key, Type.SCALAR);
    }

    @Override
    public JsonList getJsonList(String key) {
        return (JsonList) getJson(key, Type.LIST);
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
    public JsonDict put(String key, Json value) {
        m.put(key, value);
        return this;
    }

    @Override
    public JsonDict remove(String key) {
        m.remove(key);
        return this;
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public Type type() {
        return Type.DICT;
    }
}
