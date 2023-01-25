package pd.codec.json;

import java.util.List;

public interface IJsonArray extends IJson, List<IJson> {

    @Override
    public default JsonType getJsonType() {
        return JsonType.ARRAY;
    }

    /**
     * @return this
     */
    public IJsonArray append(IJson value);

    public IJson get(int index);

    public IJson getAndRemove(int index);

    /**
     * index in [0, size()]
     *
     * @return this
     */
    public IJsonArray insert(int index, IJson value);

    /**
     * @return this
     */
    public IJsonArray remove(int index);

    /**
     * @return this
     */
    public IJsonArray set(int index, IJson value);

    @Override
    public int size();
}
