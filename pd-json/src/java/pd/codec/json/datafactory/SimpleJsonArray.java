package pd.codec.json.datafactory;

import java.util.ArrayList;

import pd.codec.json.datatype.Json;
import pd.codec.json.datatype.JsonArray;

final class SimpleJsonArray extends ArrayList<Json> implements JsonArray {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    SimpleJsonArray() {
        super();
    }

    @Override
    public SimpleJsonArray append(Json value) {
        add(value);
        return this;
    }

    @Override
    public Json get(int index) {
        return super.get(index);
    }

    @Override
    public Json getAndRemove(int index) {
        return super.remove(index);
    }

    @Override
    public SimpleJsonArray insert(int index, Json value) {
        add(index, value);
        return this;
    }

    @Override
    public JsonArray remove(int index) {
        super.remove(index);
        return this;
    }

    @Override
    public SimpleJsonArray set(int index, Json value) {
        super.set(index, value);
        return this;
    }
}
