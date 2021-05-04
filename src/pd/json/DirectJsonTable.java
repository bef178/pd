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
    public DirectJsonTable put(String key, IJsonToken value) {
        super.put(key, value);
        return this;
    }

    @Override
    public DirectJsonTable remove(String key) {
        super.remove(key);
        return this;
    }
}
