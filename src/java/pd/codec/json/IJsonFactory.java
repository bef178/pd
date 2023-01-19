package pd.codec.json;

public interface IJsonFactory {

    public IJsonArray createJsonArray();

    public IJsonBoolean createJsonBoolean();

    default IJsonBoolean createJsonBoolean(boolean value) {
        return createJsonBoolean().set(value);
    }

    public IJsonNumber createJsonNumber();

    default IJsonNumber createJsonNumber(double value) {
        return createJsonNumber().set(value);
    }

    default IJsonNumber createJsonNumber(long value) {
        return createJsonNumber().set(value);
    }

    public IJsonObject createJsonObject();

    public IJsonString createJsonString();

    default IJsonString createJsonString(String value) {
        return createJsonString().set(value);
    }

    public IJsonNull getJsonNull();
}
