package pd.codec.json.datafactory;

import java.util.ArrayList;

import pd.codec.json.datatype.IJson;
import pd.codec.json.datatype.IJsonArray;

final class SimpleJsonArray extends ArrayList<IJson> implements IJsonArray {

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
    public IJson getAndRemove(int index) {
        return super.remove(index);
    }

    @Override
    public SimpleJsonArray insert(int index, IJson value) {
        add(index, value);
        return this;
    }

    @Override
    public IJsonArray remove(int index) {
        super.remove(index);
        return this;
    }

    @Override
    public SimpleJsonArray set(int index, IJson value) {
        super.set(index, value);
        return this;
    }
}
