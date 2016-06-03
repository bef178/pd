package τ.typedef.json;

public interface JsonMapping extends Json {

    /**
     * @return this object
     */
    public JsonMapping clear();

    public JsonMapping getMapping(String key);

    public JsonScalar getScalar(String key);

    public JsonSequence getSequence(String key);

    public boolean isEmpty();

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
