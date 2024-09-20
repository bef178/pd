package pd.json.datatype;

import java.util.List;

public interface JsonArray extends Json, List<Json> {

    @Override
    default JsonType getJsonType() {
        return JsonType.ARRAY;
    }

    /**
     * @return this
     */
    JsonArray append(Json value);

    Json get(int index);

    Json getAndRemove(int index);

    /**
     * index in [0, size()]
     *
     * @return this
     */
    JsonArray insert(int index, Json value);

    /**
     * @return this
     */
    JsonArray remove(int index);

    /**
     * @return this
     */
    JsonArray set(int index, Json value);

    @Override
    int size();
}
