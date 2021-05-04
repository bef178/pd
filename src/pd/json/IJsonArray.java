package pd.json;

import java.util.Collection;

public interface IJsonArray extends IJsonToken, Collection<IJsonToken> {

    public IJsonToken get(int index);

    /**
     * @return this
     */
    public IJsonArray insert(IJsonToken value);

    /**
     * @return this
     */
    public IJsonArray insert(int index, IJsonToken value);

    /**
     * @return this
     */
    public IJsonArray remove(int index);

    /**
     * @return this
     */
    public IJsonArray set(int index, IJsonToken value);
}
