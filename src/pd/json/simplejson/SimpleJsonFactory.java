package pd.json.simplejson;

import pd.json.IJsonFactory;

public class SimpleJsonFactory implements IJsonFactory {

    @Override
    public SimpleJsonArray newJsonArray() {
        return new SimpleJsonArray();
    }

    @Override
    public SimpleJsonBoolean newJsonBoolean(boolean value) {
        return value
                ? SimpleJsonBoolean.jsonTrue
                : SimpleJsonBoolean.jsonFalse;
    }

    @Override
    public SimpleJsonNull newJsonNull() {
        return SimpleJsonNull.jsonNull;
    }

    @Override
    public SimpleJsonNumber newJsonNumber() {
        return new SimpleJsonNumber();
    }

    @Override
    public SimpleJsonObject newJsonObject() {
        return new SimpleJsonObject();
    }

    @Override
    public SimpleJsonString newJsonString() {
        return new SimpleJsonString();
    }
}
