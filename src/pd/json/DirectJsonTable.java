package pd.json;

import java.util.LinkedHashMap;

class DirectJsonTable extends LinkedHashMap<String, IJsonToken> implements IJsonTable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    DirectJsonTable() {
        super();
    }

    @Override
    public IJsonToken get(String key) {
        return super.get(key);
    }

    @Override
    public DirectJsonTable put(String key, boolean value) {
        return put(key, new DirectJsonBoolean(value));
    }

    @Override
    public DirectJsonTable put(String key, double value) {
        return put(key, new DirectJsonFloat(value));
    }

    @Override
    public DirectJsonTable put(String key, IJsonToken value) {
        super.put(key, value);
        return this;
    }

    @Override
    public DirectJsonTable put(String key, long value) {
        return put(key, new DirectJsonInt(value));
    }

    @Override
    public DirectJsonTable put(String key, String value) {
        return put(key, new DirectJsonString(value));
    }

    @Override
    public DirectJsonTable remove(String key) {
        super.remove(key);
        return this;
    }
}
