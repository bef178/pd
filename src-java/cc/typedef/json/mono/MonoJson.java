package cc.typedef.json.mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import cc.typedef.json.IllegalTypeException;
import cc.typedef.json.Json;
import cc.typedef.json.JsonDict;
import cc.typedef.json.JsonList;
import cc.typedef.json.JsonScalar;

class MonoJson extends AbsMonoJson {

    private String scalar;
    private ArrayList<AbsMonoJson> sequence;
    private HashMap<String, AbsMonoJson> mapping;

    MonoJson(Type type) {
        super(type);
        switch (type) {
            case SCALAR:
                break;
            case LIST:
                sequence = new ArrayList<>();
                break;
            case DICT:
                mapping = new HashMap<>();
                break;
            default:
                throw new IllegalTypeException();
        }
    }

    @Override
    public AbsMonoJson clear() {
        if (scalar != null) {
            scalar = null;
        }
        if (sequence != null) {
            sequence.clear();
        }
        if (mapping != null) {
            mapping.clear();
        }
        return this;
    }

    @Override
    public AbsMonoJson getJson(int index) {
        checkType(Type.LIST);
        return sequence.get(index);
    }

    private AbsMonoJson getJson(int index, Type t) {
        AbsMonoJson j = getJson(index);
        if (j != null && j.type() == t) {
            return j;
        }
        throw new IllegalTypeException();
    }

    @Override
    public AbsMonoJson getJson(String key) {
        checkType(Type.DICT);
        return mapping.get(key);
    }

    private AbsMonoJson getJson(String key, Type t) {
        AbsMonoJson j = getJson(key);
        if (j != null && j.type() == t) {
            return j;
        }
        throw new IllegalTypeException();
    }

    @Override
    public JsonDict getJsonDict(int index) {
        return getJson(index, Type.DICT);
    }

    @Override
    public JsonDict getJsonDict(String key) {
        return getJson(key, Type.DICT);
    }

    @Override
    public JsonScalar getJsonScalar(int index) {
        return getJson(index, Type.SCALAR);
    }

    @Override
    public JsonScalar getJsonScalar(String key) {
        return getJson(key, Type.SCALAR);
    }

    @Override
    public JsonList getJsonList(int index) {
        return getJson(index, Type.LIST);
    }

    @Override
    public JsonList getJsonList(String key) {
        return getJson(key, Type.LIST);
    }

    @Override
    public String getString() {
        checkType(Type.SCALAR);
        return scalar;
    }

    @Override
    public JsonList insert(int index, Json value) {
        checkType(Type.LIST);
        checkInstance(value);
        sequence.add(index, (AbsMonoJson) value);
        return this;
    }

    @Override
    public JsonList insert(Json value) {
        checkType(Type.LIST);
        checkInstance(value);
        sequence.add((AbsMonoJson) value);
        return this;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Set<String> keys() {
        checkType(Type.DICT);
        return mapping.keySet();
    }

    @Override
    public JsonDict put(String key, Json value) {
        checkType(Type.DICT);
        checkInstance(value);
        mapping.put(key, (AbsMonoJson) value);
        return this;
    }

    @Override
    public JsonList remove(int index) {
        checkType(Type.LIST);
        sequence.remove(index);
        return this;
    }

    @Override
    public AbsMonoJson remove(String key) {
        checkType(Type.DICT);
        mapping.remove(key);
        return this;
    }

    @Override
    public JsonList set(int index, Json value) {
        checkType(Type.LIST);
        checkInstance(value);
        sequence.set(index, (AbsMonoJson) value);
        return this;
    }

    @Override
    public JsonScalar set(String s) {
        checkType(Type.SCALAR);
        this.scalar = s;
        return this;
    }

    @Override
    public int size() {
        switch (type()) {
            case LIST:
                return sequence.size();
            case DICT:
                return mapping.size();
            default:
                throw new IllegalTypeException();
        }
    }
}
