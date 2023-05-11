package pd.codec.json.datatype;

import java.util.List;

import pd.codec.json.JsonType;

public interface IJsonArray extends IJson, List<IJson> {

    @Override
    default JsonType getJsonType() {
        return JsonType.ARRAY;
    }

    /**
     * @return this
     */
    IJsonArray append(IJson value);

    IJson get(int index);

    IJson getAndRemove(int index);

    /**
     * index in [0, size()]
     *
     * @return this
     */
    IJsonArray insert(int index, IJson value);

    /**
     * @return this
     */
    IJsonArray remove(int index);

    /**
     * @return this
     */
    IJsonArray set(int index, IJson value);

    @Override
    int size();
}
