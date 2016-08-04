package t.typedef.json.mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import t.typedef.json.Json;
import t.typedef.json.JsonMapping;
import t.typedef.json.JsonScalar;
import t.typedef.json.JsonSequence;

class MonoJson extends AbsMonoJson {

    private String scalar;
    private ArrayList<AbsMonoJson> sequence;
    private HashMap<String, AbsMonoJson> mapping;

    MonoJson(Type type) {
        super(type);
        switch (type) {
            case SCALAR:
                break;
            case SEQUENCE:
                sequence = new ArrayList<>();
                break;
            case MAPPING:
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
        checkType(Type.SEQUENCE);
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
        checkType(Type.MAPPING);
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
    public JsonMapping getMapping(int index) {
        return getJson(index, Type.MAPPING);
    }

    @Override
    public JsonMapping getMapping(String key) {
        return getJson(key, Type.MAPPING);
    }

    @Override
    public JsonScalar getScalar(int index) {
        return getJson(index, Type.SCALAR);
    }

    @Override
    public JsonScalar getScalar(String key) {
        return getJson(key, Type.SCALAR);
    }

    @Override
    public JsonSequence getSequence(int index) {
        return getJson(index, Type.SEQUENCE);
    }

    @Override
    public JsonSequence getSequence(String key) {
        return getJson(key, Type.SEQUENCE);
    }

    @Override
    public String getString() {
        checkType(Type.SCALAR);
        return scalar;
    }

    @Override
    public JsonSequence insert(int index, Json value) {
        checkType(Type.SEQUENCE);
        checkInstance(value);
        sequence.add(index, (AbsMonoJson) value);
        return this;
    }

    @Override
    public JsonSequence insert(Json value) {
        checkType(Type.SEQUENCE);
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
        checkType(Type.MAPPING);
        return mapping.keySet();
    }

    @Override
    public JsonMapping put(String key, Json value) {
        checkType(Type.MAPPING);
        checkInstance(value);
        mapping.put(key, (AbsMonoJson) value);
        return this;
    }

    @Override
    public JsonSequence remove(int index) {
        checkType(Type.SEQUENCE);
        sequence.remove(index);
        return this;
    }

    @Override
    public AbsMonoJson remove(String key) {
        checkType(Type.MAPPING);
        mapping.remove(key);
        return this;
    }

    @Override
    public JsonSequence set(int index, Json value) {
        checkType(Type.SEQUENCE);
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
            case SEQUENCE:
                return sequence.size();
            case MAPPING:
                return mapping.size();
            default:
                throw new IllegalTypeException();
        }
    }
}
