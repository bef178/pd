package pd.codec.json.datatype;

import pd.codec.json.JsonType;

public interface JsonBoolean extends Json {

    boolean getBoolean();

    @Override
    default JsonType getJsonType() {
        return JsonType.BOOLEAN;
    }

    JsonBoolean set(boolean value);
}
