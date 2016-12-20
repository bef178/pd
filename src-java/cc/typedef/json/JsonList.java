package cc.typedef.json;

public interface JsonList extends Json {

    /**
     * @return this object
     */
    public JsonList clear();

    public Json getJson(int index);

    public JsonDict getJsonDict(int index);

    public JsonList getJsonList(int index);

    public JsonScalar getJsonScalar(int index);

    /**
     * @return this object
     */
    public JsonList insert(int index, Json value);

    /**
     * @return this object
     */
    public JsonList insert(Json value);

    public boolean isEmpty();

    /**
     * @return this object
     */
    public JsonList remove(int index);

    /**
     * @return this object
     */
    public JsonList set(int index, Json value);

    public int size();
}
