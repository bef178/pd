package pd.codec.json.datatype;

import pd.codec.json.JsonType;

public interface JsonString extends Json {

    @Override
    default JsonType getJsonType() {
        return JsonType.STRING;
    }

    String getString();

    JsonString set(String value);
}
