package pd.codec.json.simplejson;

import pd.codec.json.IJsonFactory;

public class SimpleJsonFactory implements IJsonFactory {

    @Override
    public SimpleJsonArray getJsonArray() {
        return new SimpleJsonArray();
    }

    @Override
    public SimpleJsonBoolean getJsonBoolean() {
        return new SimpleJsonBoolean();
    }

    @Override
    public SimpleJsonBoolean getJsonBoolean(boolean value) {
        return new SimpleJsonBoolean().set(value);
    }

    @Override
    public SimpleJsonNull getJsonNull() {
        return SimpleJsonNull.NULL;
    }

    @Override
    public SimpleJsonNumber getJsonNumber() {
        return new SimpleJsonNumber();
    }

    @Override
    public SimpleJsonObject getJsonObject() {
        return new SimpleJsonObject();
    }

    @Override
    public SimpleJsonString getJsonString() {
        return new SimpleJsonString();
    }
}
