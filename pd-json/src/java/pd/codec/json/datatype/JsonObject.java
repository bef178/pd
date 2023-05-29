package pd.codec.json.datatype;

import java.util.Map;

import pd.codec.json.JsonType;

public interface JsonObject extends Json, Map<String, Json> {

    @Override
    default JsonType getJsonType() {
        return JsonType.OBJECT;
    }

    Json get(String key);

    Json getAndRemove(String key);

    /**
     * @return this
     */
    JsonObject remove(String key);

    /**
     * @return this
     */
    JsonObject set(String key, Json value);

    @Override
    int size();
}
