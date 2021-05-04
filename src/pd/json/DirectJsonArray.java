package pd.json;

import java.util.ArrayList;

class DirectJsonArray extends ArrayList<IJsonToken> implements IJsonArray {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    DirectJsonArray() {
        super();
    }

    @Override
    public IJsonToken get(int index) {
        return super.get(index);
    }

    @Override
    public DirectJsonArray insert(IJsonToken value) {
        add(value);
        return this;
    }

    @Override
    public DirectJsonArray insert(int index, boolean value) {
        return insert(index, new DirectJsonBoolean(value));
    }

    @Override
    public DirectJsonArray insert(int index, double value) {
        return insert(index, new DirectJsonFloat(value));
    }

    @Override
    public DirectJsonArray insert(int index, IJsonToken value) {
        add(index, value);
        return this;
    }

    @Override
    public DirectJsonArray insert(int index, long value) {
        return insert(index, new DirectJsonInt(value));
    }

    @Override
    public DirectJsonArray insert(int index, String value) {
        return insert(index, new DirectJsonString(value));
    }

    @Override
    public DirectJsonArray remove(int index) {
        super.remove(index);
        return this;
    }

    @Override
    public DirectJsonArray set(int index, IJsonToken value) {
        super.set(index, value);
        return this;
    }
}
