package pd.codec.json;

public interface IJsonFactory {

    public IJsonArray getJsonArray();

    public IJsonBoolean getJsonBoolean();

    public IJsonBoolean getJsonBoolean(boolean value);

    public IJsonNull getJsonNull();

    public IJsonNumber getJsonNumber();

    public IJsonObject getJsonObject();

    public IJsonString getJsonString();
}
