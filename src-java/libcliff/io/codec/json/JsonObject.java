package libcliff.io.codec.json;

import java.util.Set;

public interface JsonObject extends Json {

    /**
     * @return this object
     */
    public JsonObject clear();

    public Json getJson(String key);

    public JsonObject getJsonObject(String key);

    public JsonScalar getJsonScalar(String key);

    public JsonVector getJsonVector(String key);

    public boolean isEmpty();

    public Set<String> keys();

    /**
     * @return this object
     */
    public JsonObject put(String key, Json value);

    /**
     * @return this object
     */
    public JsonObject remove(String key);

    public int size();
}
