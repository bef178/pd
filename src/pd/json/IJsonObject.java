package pd.json;

import java.util.Map;

public interface IJsonObject extends IJsonToken, Map<String, IJsonToken> {

    public IJsonToken get(String key);

    public IJsonObject put(String key, boolean value);

    public IJsonObject put(String key, double value);

    /**
     * @return this
     */
    @Override
    public IJsonObject put(String key, IJsonToken value);

    public IJsonObject put(String key, long value);

    public IJsonObject put(String key, String value);

    /**
     * @return this
     */
    public IJsonObject remove(String key);
}
