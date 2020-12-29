package pd.fenc.json;

public interface IJsonCreator {

    public <T extends IJsonValue> T cast(IJsonValue value, Class<T> expectedClass);

    public IJsonArray newJsonArray();

    public IJsonBoolean newJsonBoolean(boolean value);

    public IJsonNull newJsonNull();

    public IJsonNumber newJsonNumber(Number value);

    public IJsonObject newJsonObject();

    public IJsonString newJsonString(String value);
}
