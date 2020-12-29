package pd.fenc.json;

import java.util.Collection;

public interface IJsonArray extends IJsonValue, Collection<IJsonValue> {

    public IJsonArray getAsJsonArray(int index);

    public IJsonBoolean getAsJsonBoolean(int index);

    public IJsonNull getAsJsonNull(int index);

    public IJsonNumber getAsJsonNumber(int index);

    public IJsonObject getAsJsonObject(int index);

    public IJsonString getAsJsonString(int index);

    public IJsonValue getAsJsonValue(int index);

    /**
     * @return this
     */
    public IJsonArray insert(IJsonValue value);

    /**
     * @return this
     */
    public IJsonArray insert(int index, IJsonValue value);

    /**
     * @return this
     */
    public IJsonArray remove(int index);

    public String serialize(String margin, String indent, String eol, int numIndents);

    /**
     * @return this
     */
    public IJsonArray set(int index, IJsonValue value);
}
