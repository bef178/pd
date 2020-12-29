package pd.fenc.json;

class ImplDirectJsonCreator implements IJsonCreator {

    @Override
    public <T extends IJsonValue> T cast(IJsonValue value, Class<T> expected) {
        return ImplDirectJsonObject.cast(value, expected);
    }

    @Override
    public ImplDirectJsonArray newJsonArray() {
        return new ImplDirectJsonArray();
    }

    @Override
    public ImplDirectJsonBoolean newJsonBoolean(boolean value) {
        return new ImplDirectJsonBoolean(value);
    }

    @Override
    public ImplDirectJsonNull newJsonNull() {
        return ImplDirectJsonNull.instance;
    }

    @Override
    public ImplDirectJsonNumber newJsonNumber(Number value) {
        return new ImplDirectJsonNumber(value);
    }

    @Override
    public ImplDirectJsonObject newJsonObject() {
        return new ImplDirectJsonObject();
    }

    @Override
    public ImplDirectJsonString newJsonString(String value) {
        return new ImplDirectJsonString(value);
    }
}
