package τ.typedef.json.mono;

import java.util.ArrayList;
import java.util.HashMap;

import τ.typedef.json.Json;
import τ.typedef.json.JsonMapping;
import τ.typedef.json.JsonScalar;
import τ.typedef.json.JsonSequence;

class MonoJson extends AbsMonoJson {

    public static class Producer implements Json.Producer {

        @Override
        public JsonMapping produceMapping() {
            return new MonoJson(Type.MAPPING);
        }

        @Override
        public JsonScalar produceScalar() {
            return new MonoJson(Type.SCALAR);
        }

        @Override
        public JsonSequence produceSequence() {
            return new MonoJson(Type.SEQUENCE);
        }
    }

    private String scalar;
    private ArrayList<AbsMonoJson> sequence;
    private HashMap<String, AbsMonoJson> mapping;

    private MonoJson(Type type) {
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

    private AbsMonoJson getJson(int index) {
        checkType(Type.SEQUENCE);
        return sequence.get(index);
    }

    private AbsMonoJson getJson(String key) {
        checkType(Type.MAPPING);
        return mapping.get(key);
    }

    @Override
    public JsonMapping getMapping(int index) {
        AbsMonoJson j = getJson(index);
        if (j != null) {
            checkType(j, Type.MAPPING);
        }
        return j;
    }

    @Override
    public JsonMapping getMapping(String key) {
        AbsMonoJson j = getJson(key);
        if (j != null) {
            checkType(j, Type.MAPPING);
        }
        return j;
    }

    @Override
    public JsonScalar getScalar(int index) {
        AbsMonoJson j = getJson(index);
        if (j != null) {
            checkType(j, Type.SCALAR);
        }
        return j;
    }

    @Override
    public JsonScalar getScalar(String key) {
        AbsMonoJson j = getJson(key);
        if (j != null) {
            checkType(j, Type.SCALAR);
        }
        return j;
    }

    @Override
    public JsonSequence getSequence(int index) {
        AbsMonoJson j = getJson(index);
        if (j != null) {
            checkType(j, Type.SEQUENCE);
        }
        return j;
    }

    @Override
    public JsonSequence getSequence(String key) {
        AbsMonoJson j = getJson(key);
        if (j != null) {
            checkType(j, Type.SEQUENCE);
        }
        return j;
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
