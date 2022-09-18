package pd.codec.json;

import java.util.Collection;

public interface IJsonArray extends IJson, Collection<IJson> {

    /**
     * @return this
     */
    public IJsonArray append(IJson value);

    public IJson get(int index);

    @Override
    public default JsonType getJsonType() {
        return JsonType.ARRAY;
    }

    /**
     * index in [0, size()]
     *
     * @return this
     */
    public IJsonArray insert(int index, IJson value);

    public IJson remove(int index);

    /**
     * @return this
     */
    public IJsonArray set(int index, IJson value);

    @Override
    public int size();
}
