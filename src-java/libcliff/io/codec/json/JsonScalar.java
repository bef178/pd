package libcliff.io.codec.json;

public interface JsonScalar extends Json {

    public Boolean getBoolean();

    public Double getDouble();

    public Integer getInteger();

    public String getString();

    /**
     * @return this object
     */
    public JsonScalar set(Boolean value);

    /**
     * @return this object
     */
    public JsonScalar set(Double value);

    /**
     * @return this object
     */
    public JsonScalar set(Integer value);

    /**
     * @return this object
     */
    public JsonScalar set(String value);
}
