package t.typedef.json.simple;

import java.util.ArrayList;

import t.typedef.json.IllegalTypeException;
import t.typedef.json.Json;
import t.typedef.json.JsonDict;
import t.typedef.json.JsonList;
import t.typedef.json.JsonScalar;

class SimpleJsonList implements JsonList {

    private ArrayList<Json> q = new ArrayList<Json>();

    SimpleJsonList() {
        // dummy
    }

    @Override
    public JsonList clear() {
        q.clear();
        return this;
    }

    @Override
    public Json getJson(int index) {
        return q.get(index);
    }

    private Json getJson(int index, Type t) {
        Json j = q.get(index);
        if (j != null && j.type() == t) {
            return j;
        }
        throw new IllegalTypeException();
    }

    @Override
    public JsonDict getJsonDict(int index) {
        return (JsonDict) getJson(index, Type.DICT);
    }

    @Override
    public JsonScalar getJsonScalar(int index) {
        return (JsonScalar) getJson(index, Type.SCALAR);
    }

    @Override
    public JsonList getJsonList(int index) {
        return (JsonList) getJson(index, Type.LIST);
    }

    @Override
    public JsonList insert(int index, Json value) {
        q.add(index, value);
        return this;
    }

    @Override
    public JsonList insert(Json value) {
        q.add(value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return q.isEmpty();
    }

    @Override
    public JsonList remove(int index) {
        q.remove(index);
        return this;
    }

    @Override
    public JsonList set(int index, Json value) {
        q.set(index, value);
        return this;
    }

    @Override
    public int size() {
        return q.size();
    }

    @Override
    public Type type() {
        return Type.LIST;
    }
}
