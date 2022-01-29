package pd.json.simplejson;

import java.util.ArrayList;

import pd.json.type.IJson;
import pd.json.type.IJsonArray;

class SimpleJsonArray extends ArrayList<IJson> implements IJsonArray {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    SimpleJsonArray() {
        super();
    }

    @Override
    public SimpleJsonArray append(IJson value) {
        add(value);
        return this;
    }

    @Override
    public IJson get(int index) {
        return super.get(index);
    }

    @Override
    public SimpleJsonArray insert(int index, IJson value) {
        add(index, value);
        return this;
    }

    @Override
    public IJson remove(int index) {
        return super.remove(index);
    }

    @Override
    public SimpleJsonArray set(int index, IJson value) {
        super.set(index, value);
        return this;
    }
}
