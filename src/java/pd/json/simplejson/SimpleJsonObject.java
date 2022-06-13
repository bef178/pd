package pd.json.simplejson;

import java.util.LinkedHashMap;

import pd.json.type.IJson;
import pd.json.type.IJsonObject;

class SimpleJsonObject extends LinkedHashMap<String, IJson> implements IJsonObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    SimpleJsonObject() {
        super();
    }

    @Override
    public IJson get(String key) {
        return super.get(key);
    }

    @Override
    public IJson remove(String key) {
        return super.remove(key);
    }

    @Override
    public SimpleJsonObject set(String key, IJson value) {
        super.put(key, value);
        return this;
    }
}
