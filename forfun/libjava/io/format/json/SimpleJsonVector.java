package libjava.io.format.json;

import static libjava.io.format.json.SimpleJsonObject.checkType;

import java.util.ArrayList;

import libjava.io.Pushable;
import libjava.io.format.json.JsonSerializer.Config;

class SimpleJsonVector implements JsonVector {

    private ArrayList<Json> l = new ArrayList<Json>();

    SimpleJsonVector() {
        // dummy
    }

    @Override
    public JsonVector clear() {
        l.clear();
        return this;
    }

    @Override
    public Json getJson(int index) {
        return l.get(index);
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return checkType(l.get(index), JsonObject.class);
    }

    @Override
    public JsonScalar getJsonScalar(int index) {
        return checkType(l.get(index), JsonScalar.class);
    }

    @Override
    public JsonVector getJsonVector(int index) {
        return checkType(l.get(index), JsonVector.class);
    }

    @Override
    public JsonVector insert(int index, Json value) {
        l.add(index, value);
        return this;
    }

    @Override
    public JsonVector insert(Json value) {
        l.add(value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return l.isEmpty();
    }

    @Override
    public JsonVector remove(int index) {
        l.remove(index);
        return this;
    }

    @Override
    public JsonVector set(int index, Json value) {
        l.set(index, value);
        return this;
    }

    @Override
    public int size() {
        return l.size();
    }

    @Override
    public void serialize(Config config, Pushable it) {
        JsonSerializer.serializeVector(this, config, it);
    }
}
