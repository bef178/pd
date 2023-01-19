package pd.codec.json;

import java.util.Map;

public interface IJsonObject extends IJson, Map<String, IJson> {

    @Override
    public default JsonType getJsonType() {
        return JsonType.OBJECT;
    }

    public IJson get(String key);

    public IJson getAndRemove(String key);

    /**
     * @return this
     */
    public IJsonObject remove(String key);

    /**
     * @return this
     */
    public IJsonObject set(String key, IJson value);

    @Override
    public int size();
}
