package libcliff.json;

import java.util.Set;

public interface JsonDict extends Json {

    /**
     * @return this object
     */
    public JsonDict clear();

    public Json getJson(String key);

    public JsonDict getJsonDict(String key);

    public JsonList getJsonList(String key);

    public JsonScalar getJsonScalar(String key);

    public boolean isEmpty();

    public Set<String> keys();

    /**
     * @return this object
     */
    public JsonDict put(String key, Json value);

    /**
     * @return this object
     */
    public JsonDict remove(String key);

    public int size();
}
