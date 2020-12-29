package pd.fenc.json;

import static pd.fenc.json.ImplDirectJsonObject.cast;
import static pd.fenc.json.JsonSerializer.serializeJsonValue;
import static pd.fenc.json.JsonSerializer.serializeMarginAndIndents;

import java.util.ArrayList;
import java.util.Iterator;

class ImplDirectJsonArray extends ArrayList<IJsonValue> implements IJsonArray {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    ImplDirectJsonArray() {
        super();
    }

    @Override
    public IJsonArray getAsJsonArray(int index) {
        return cast(get(index), IJsonArray.class);
    }

    @Override
    public IJsonBoolean getAsJsonBoolean(int index) {
        return cast(get(index), IJsonBoolean.class);
    }

    @Override
    public IJsonNull getAsJsonNull(int index) {
        return cast(get(index), IJsonNull.class);
    }

    @Override
    public IJsonNumber getAsJsonNumber(int index) {
        return cast(get(index), IJsonNumber.class);
    }

    @Override
    public IJsonObject getAsJsonObject(int index) {
        return cast(get(index), IJsonObject.class);
    }

    @Override
    public IJsonString getAsJsonString(int index) {
        return cast(get(index), IJsonString.class);
    }

    @Override
    public IJsonValue getAsJsonValue(int index) {
        return get(index);
    }

    @Override
    public ImplDirectJsonArray insert(IJsonValue value) {
        add(value);
        return this;
    }

    @Override
    public ImplDirectJsonArray insert(int index, IJsonValue value) {
        add(index, value);
        return this;
    }

    @Override
    public ImplDirectJsonArray remove(int index) {
        super.remove(index);
        return this;
    }

    @Override
    public String serialize() {
        return serialize("", "", "", 0);
    }

    @Override
    public String serialize(String margin, String indent, String eol, int numIndents) {

        if (margin == null) {
            margin = "";
        }

        if (indent == null) {
            indent = "";
        }

        if (eol == null) {
            eol = "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append('[');

        if (!isEmpty()) {
            sb.append(eol);
        }

        numIndents++;

        Iterator<IJsonValue> it = iterator();
        while (it.hasNext()) {
            IJsonValue value = it.next();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            serializeJsonValue(value, margin, indent, eol, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(eol);
        }

        numIndents--;

        if (!isEmpty()) {
            serializeMarginAndIndents(margin, indent, numIndents, sb);
        }

        sb.append(']');

        return sb.toString();
    }

    @Override
    public IJsonArray set(int index, IJsonValue value) {
        super.set(index, value);
        return this;
    }
}
