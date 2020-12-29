package pd.fenc.json;

import static pd.fenc.json.JsonSerializer.serializeJsonValue;
import static pd.fenc.json.JsonSerializer.serializeMarginAndIndents;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import pd.fenc.ParsingException;

class ImplDirectJsonObject extends LinkedHashMap<String, IJsonValue> implements IJsonObject {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    static <T extends IJsonValue> T cast(IJsonValue value, Class<T> expectedClass) {
        if (value == null || !expectedClass.isInstance(value)) {
            throw new ParsingException(
                    String.format("invalid json type: expected [%s], actual [%s]",
                            expectedClass.getName(), value.getClass().getName()));
        }
        return expectedClass.cast(value);
    }

    ImplDirectJsonObject() {
        // dummy
    }

    @Override
    public IJsonArray getAsJsonArray(String key) {
        return cast(get(key), IJsonArray.class);
    }

    @Override
    public IJsonBoolean getAsJsonBoolean(String key) {
        return cast(get(key), IJsonBoolean.class);
    }

    @Override
    public IJsonNull getAsJsonNull(String key) {
        return cast(get(key), IJsonNull.class);
    }

    @Override
    public IJsonNumber getAsJsonNumber(String key) {
        return cast(get(key), IJsonNumber.class);
    }

    @Override
    public IJsonObject getAsJsonObject(String key) {
        return cast(get(key), IJsonObject.class);
    }

    @Override
    public IJsonString getAsJsonString(String key) {
        return cast(get(key), IJsonString.class);
    }

    @Override
    public IJsonValue getAsJsonValue(String key) {
        return get(key);
    }

    @Override
    public Set<String> keys() {
        return keySet();
    }

    @Override
    public ImplDirectJsonObject put(String key, IJsonValue value) {
        super.put(key, value);
        return this;
    }

    @Override
    public ImplDirectJsonObject remove(String key) {
        super.remove(key);
        return this;
    }

    /**
     * cheat sheet style
     */
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

        sb.append('{');

        if (!isEmpty()) {
            sb.append(eol);
        }

        numIndents++;

        Iterator<Entry<String, IJsonValue>> it = entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, IJsonValue> entry = it.next();
            String key = entry.getKey();
            IJsonValue value = entry.getValue();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            sb.append(ImplDirectJsonString.serialize(key));
            sb.append(':');
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

        sb.append('}');

        return sb.toString();
    }
}
