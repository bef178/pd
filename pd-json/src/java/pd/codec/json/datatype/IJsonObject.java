package pd.codec.json.datatype;

import java.util.Map;

import pd.codec.json.JsonType;

public interface IJsonObject extends IJson, Map<String, IJson> {

    @Override
    default JsonType getJsonType() {
        return JsonType.OBJECT;
    }

    IJson get(String key);

    IJson getAndRemove(String key);

    /**
     * @return this
     */
    IJsonObject remove(String key);

    /**
     * @return this
     */
    IJsonObject set(String key, IJson value);

    @Override
    int size();
}
