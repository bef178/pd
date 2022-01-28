package pd.json;

import java.util.LinkedHashMap;

class DirectJsonObject extends LinkedHashMap<String, IJsonToken> implements IJsonObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    DirectJsonObject() {
        super();
    }

    @Override
    public IJsonToken get(String key) {
        return super.get(key);
    }

    @Override
    public DirectJsonObject put(String key, boolean value) {
        return put(key, new DirectJsonBoolean(value));
    }

    @Override
    public DirectJsonObject put(String key, double value) {
        return put(key, new DirectJsonFloat(value));
    }

    @Override
    public DirectJsonObject put(String key, IJsonToken value) {
        super.put(key, value);
        return this;
    }

    @Override
    public DirectJsonObject put(String key, long value) {
        return put(key, new DirectJsonInt(value));
    }

    @Override
    public DirectJsonObject put(String key, String value) {
        return put(key, new DirectJsonString(value));
    }

    @Override
    public DirectJsonObject remove(String key) {
        super.remove(key);
        return this;
    }
}
