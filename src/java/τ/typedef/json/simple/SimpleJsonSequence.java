package τ.typedef.json.simple;

import java.util.ArrayList;

import τ.typedef.json.Json;
import τ.typedef.json.JsonMapping;
import τ.typedef.json.JsonScalar;
import τ.typedef.json.JsonSequence;

class SimpleJsonSequence implements JsonSequence {

    private ArrayList<Json> q = new ArrayList<Json>();

    SimpleJsonSequence() {
        // dummy
    }

    @Override
    public JsonSequence clear() {
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
    public JsonMapping getMapping(int index) {
        return (JsonMapping) getJson(index, Type.MAPPING);
    }

    @Override
    public JsonScalar getScalar(int index) {
        return (JsonScalar) getJson(index, Type.SCALAR);
    }

    @Override
    public JsonSequence getSequence(int index) {
        return (JsonSequence) getJson(index, Type.SEQUENCE);
    }

    @Override
    public JsonSequence insert(int index, Json value) {
        q.add(index, value);
        return this;
    }

    @Override
    public JsonSequence insert(Json value) {
        q.add(value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return q.isEmpty();
    }

    @Override
    public JsonSequence remove(int index) {
        q.remove(index);
        return this;
    }

    @Override
    public JsonSequence set(int index, Json value) {
        q.set(index, value);
        return this;
    }

    @Override
    public int size() {
        return q.size();
    }

    @Override
    public Type type() {
        return Type.SEQUENCE;
    }
}
