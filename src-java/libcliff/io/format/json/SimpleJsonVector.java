package libcliff.io.format.json;

import java.util.ArrayList;

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

    private Json getJson(int index, JsonType valueType) {
        return Json.checkType(l.get(index), valueType);
    }

    @Override
    public JsonObject getJsonObject(int index) {
        return (JsonObject) getJson(index, JsonType.OBJECT);
    }

    @Override
    public JsonScalar getJsonScalar(int index) {
        return (JsonScalar) getJson(index, JsonType.SCALAR);
    }

    @Override
    public JsonVector getJsonVector(int index) {
        return (JsonVector) getJson(index, JsonType.VECTOR);
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
    public JsonType type() {
        return JsonType.VECTOR;
    }
}
