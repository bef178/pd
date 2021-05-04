package pd.json;

import java.util.Map;

public interface IJsonTable extends IJsonToken, Map<String, IJsonToken> {

    public IJsonToken get(String key);

    /**
     * @return this
     */
    @Override
    public IJsonTable put(String key, IJsonToken value);

    /**
     * @return this
     */
    public IJsonTable remove(String key);
}
