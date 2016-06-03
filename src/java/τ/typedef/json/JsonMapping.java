package τ.typedef.json;

import java.util.Set;

public interface JsonMapping extends Json {

    /**
     * @return this object
     */
    public JsonMapping clear();

    public Json getJson(String key);

    public JsonMapping getMapping(String key);

    public JsonScalar getScalar(String key);

    public JsonSequence getSequence(String key);

    public boolean isEmpty();

    public Set<String> keys();

    /**
     * @return this object
     */
    public JsonMapping put(String key, Json value);

    /**
     * @return this object
     */
    public JsonMapping remove(String key);

    public int size();
}
