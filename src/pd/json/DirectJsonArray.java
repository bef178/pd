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
    public DirectJsonArray insert(int index, IJsonToken value) {
        add(index, value);
        return this;
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
