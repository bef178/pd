package pd.json.datatype;

import java.io.Serializable;

public interface Json extends Serializable {

    default JsonArray asJsonArray() {
        return getJsonType() == JsonType.ARRAY
                ? (JsonArray) this
                : null;
    }

    default JsonBoolean asJsonBoolean() {
        return getJsonType() == JsonType.BOOLEAN
                ? (JsonBoolean) this
                : null;
    }

    default JsonNull asJsonNull() {
        return getJsonType() == JsonType.NULL
                ? (JsonNull) this
                : null;
    }

    default JsonNumber asJsonNumber() {
        return getJsonType() == JsonType.NUMBER
                ? (JsonNumber) this
                : null;
    }

    default JsonObject asJsonObject() {
        return getJsonType() == JsonType.OBJECT
                ? (JsonObject) this
                : null;
    }

    default JsonString asJsonString() {
        return getJsonType() == JsonType.STRING
                ? (JsonString) this
                : null;
    }

    JsonType getJsonType();

    enum JsonType {
        OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL;
    }
}
