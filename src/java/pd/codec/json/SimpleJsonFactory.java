package pd.codec.json;

final class SimpleJsonFactory implements IJsonFactory {

    @Override
    public SimpleJsonArray createJsonArray() {
        return new SimpleJsonArray();
    }

    @Override
    public SimpleJsonBoolean createJsonBoolean() {
        return new SimpleJsonBoolean();
    }

    @Override
    public SimpleJsonNumber createJsonNumber() {
        return new SimpleJsonNumber();
    }

    @Override
    public SimpleJsonObject createJsonObject() {
        return new SimpleJsonObject();
    }

    @Override
    public SimpleJsonString createJsonString() {
        return new SimpleJsonString();
    }

    @Override
    public SimpleJsonNull getJsonNull() {
        return SimpleJsonNull.NULL;
    }
}
