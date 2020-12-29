package pd.fenc.json;

import java.util.Collection;
import java.util.Map;

public interface IJsonObject extends IJsonValue, Map<String, IJsonValue> {

    public IJsonArray getAsJsonArray(String key);

    public IJsonBoolean getAsJsonBoolean(String key);

    public IJsonNull getAsJsonNull(String key);

    public IJsonNumber getAsJsonNumber(String key);

    public IJsonObject getAsJsonObject(String key);

    public IJsonString getAsJsonString(String key);

    public IJsonValue getAsJsonValue(String key);

    public Collection<String> keys();

    /**
     * @param this
     */
    public IJsonObject put(String key, IJsonValue value);

    /**
     * @param this
     */
    public IJsonObject remove(String key);

    public String serialize(String margin, String indent, String eol, int numIndents);
}
