package τ.typedef.json;

public interface JsonSequence extends Json {

    /**
     * @return this object
     */
    public JsonSequence clear();

    public JsonMapping getMapping(int index);

    public JsonSequence getSequence(int index);

    public JsonScalar getScalar(int index);

    /**
     * @return this object
     */
    public JsonSequence insert(int index, Json value);

    /**
     * @return this object
     */
    public JsonSequence insert(Json value);

    public boolean isEmpty();

    /**
     * @return this object
     */
    public JsonSequence remove(int index);

    /**
     * @return this object
     */
    public JsonSequence set(int index, Json value);

    public int size();
}
