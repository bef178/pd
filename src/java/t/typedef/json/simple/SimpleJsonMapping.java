package t.typedef.json.simple;

import java.util.HashMap;
import java.util.Set;

import t.typedef.json.Json;
import t.typedef.json.JsonMapping;
import t.typedef.json.JsonScalar;
import t.typedef.json.JsonSequence;

class SimpleJsonMapping implements JsonMapping {

    private HashMap<String, Json> m = new HashMap<>();

    SimpleJsonMapping() {
        // dummy
    }

    @Override
    public JsonMapping clear() {
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
    public JsonMapping getMapping(String key) {
        return (JsonMapping) getJson(key, Type.MAPPING);
    }

    @Override
    public JsonScalar getScalar(String key) {
        return (JsonScalar) getJson(key, Type.SCALAR);
    }

    @Override
    public JsonSequence getSequence(String key) {
        return (JsonSequence) getJson(key, Type.SEQUENCE);
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
    public JsonMapping put(String key, Json value) {
        m.put(key, value);
        return this;
    }

    @Override
    public JsonMapping remove(String key) {
        m.remove(key);
        return this;
    }

    @Override
    public int size() {
        return m.size();
    }

    @Override
    public Type type() {
        return Type.MAPPING;
    }
}
