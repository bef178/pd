package pd.codec.json.datatype;

import java.io.Serializable;

import pd.codec.json.JsonType;

public interface IJson extends Serializable {

    default IJsonArray asJsonArray() {
        return getJsonType() == JsonType.ARRAY
                ? IJsonArray.class.cast(this)
                : null;
    }

    default IJsonBoolean asJsonBoolean() {
        return getJsonType() == JsonType.BOOLEAN
                ? IJsonBoolean.class.cast(this)
                : null;
    }

    default IJsonNull asJsonNull() {
        return getJsonType() == JsonType.NULL
                ? IJsonNull.class.cast(this)
                : null;
    }

    default IJsonNumber asJsonNumber() {
        return getJsonType() == JsonType.NUMBER
                ? IJsonNumber.class.cast(this)
                : null;
    }

    default IJsonObject asJsonObject() {
        return getJsonType() == JsonType.OBJECT
                ? IJsonObject.class.cast(this)
                : null;
    }

    default IJsonString asJsonString() {
        return getJsonType() == JsonType.STRING
                ? IJsonString.class.cast(this)
                : null;
    }

    JsonType getJsonType();
}
