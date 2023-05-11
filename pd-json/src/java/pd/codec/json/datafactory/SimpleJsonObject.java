package pd.codec.json.datafactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.datatype.IJson;
import pd.codec.json.datatype.IJsonObject;

final class SimpleJsonObject extends LinkedHashMap<String, IJson> implements IJsonObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private static final Comparator<Map.Entry<String, IJson>> comparator = Map.Entry.comparingByKey();

    public SimpleJsonObject() {
        super();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (o instanceof SimpleJsonObject) {
            return equalsIgnoreOrder((SimpleJsonObject) o);
        }
        return false;
    }

    public boolean equalsIgnoreOrder(Map<String, IJson> o) {
        if (o == this) {
            return true;
        }
        if (o == null) {
            return false;
        }
        List<Map.Entry<String, IJson>> l = new ArrayList<>(o.entrySet());
        l.sort(comparator);
        List<Map.Entry<String, IJson>> a = new ArrayList<>(entrySet());
        a.sort(comparator);
        return l.equals(a);
    }

    @Override
    public IJson get(String key) {
        return super.get(key);
    }

    @Override
    public IJson getAndRemove(String key) {
        return super.remove(key);
    }

    @Override
    public IJsonObject remove(String key) {
        super.remove(key);
        return this;
    }

    @Override
    public SimpleJsonObject set(String key, IJson value) {
        super.put(key, value);
        return this;
    }
}
