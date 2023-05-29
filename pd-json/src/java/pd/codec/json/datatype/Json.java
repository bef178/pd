package pd.codec.json.datatype;

import java.io.Serializable;

import pd.codec.json.JsonType;

public interface Json extends Serializable {

    default JsonArray asJsonArray() {
        return getJsonType() == JsonType.ARRAY
                ? JsonArray.class.cast(this)
                : null;
    }

    default JsonBoolean asJsonBoolean() {
        return getJsonType() == JsonType.BOOLEAN
                ? JsonBoolean.class.cast(this)
                : null;
    }

    default JsonNull asJsonNull() {
        return getJsonType() == JsonType.NULL
                ? JsonNull.class.cast(this)
                : null;
    }

    default JsonNumber asJsonNumber() {
        return getJsonType() == JsonType.NUMBER
                ? JsonNumber.class.cast(this)
                : null;
    }

    default JsonObject asJsonObject() {
        return getJsonType() == JsonType.OBJECT
                ? JsonObject.class.cast(this)
                : null;
    }

    default JsonString asJsonString() {
        return getJsonType() == JsonType.STRING
                ? JsonString.class.cast(this)
                : null;
    }

    JsonType getJsonType();
}
