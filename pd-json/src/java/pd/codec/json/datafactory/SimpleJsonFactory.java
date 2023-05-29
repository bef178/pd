package pd.codec.json.datafactory;

final class SimpleJsonFactory implements JsonFactory {

    private static SimpleJsonFactory one = new SimpleJsonFactory();

    public static SimpleJsonFactory singleton() {
        return one;
    }

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
