package pd.json;

import java.util.Map;

public interface IJsonTable extends IJsonToken, Map<String, IJsonToken> {

    public IJsonToken get(String key);

    public IJsonTable put(String key, boolean value);

    public IJsonTable put(String key, double value);

    /**
     * @return this
     */
    @Override
    public IJsonTable put(String key, IJsonToken value);

    public IJsonTable put(String key, long value);

    public IJsonTable put(String key, String value);

    /**
     * @return this
     */
    public IJsonTable remove(String key);
}
