package pd.codec.json;

import java.util.LinkedHashMap;

final class SimpleJsonObject extends LinkedHashMap<String, IJson> implements IJsonObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SimpleJsonObject() {
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