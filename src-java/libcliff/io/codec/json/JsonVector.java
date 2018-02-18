package libcliff.io.codec.json;

public interface JsonVector extends Json {

    /**
     * @return this object
     */
    public JsonVector clear();

    public Json getJson(int index);

    public JsonObject getJsonObject(int index);

    public JsonScalar getJsonScalar(int index);

    public JsonVector getJsonVector(int index);

    /**
     * @return this object
     */
    public JsonVector insert(int index, Json value);

    /**
     * @return this object
     */
    public JsonVector insert(Json value);

    public boolean isEmpty();

    /**
     * @return this object
     */
    public JsonVector remove(int index);

    /**
     * @return this object
     */
    public JsonVector set(int index, Json value);

    public int size();
}
