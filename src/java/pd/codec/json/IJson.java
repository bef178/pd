package pd.codec.json;

import java.io.Serializable;

public interface IJson extends Serializable {

    public default IJsonArray asJsonArray() {
        return getJsonType() == JsonType.ARRAY
                ? IJsonArray.class.cast(this)
                : null;
    }

    public default IJsonBoolean asJsonBoolean() {
        return getJsonType() == JsonType.BOOLEAN
                ? IJsonBoolean.class.cast(this)
                : null;
    }

    public default IJsonNull asJsonNull() {
        return getJsonType() == JsonType.NULL
                ? IJsonNull.class.cast(this)
                : null;
    }

    public default IJsonNumber asJsonNumber() {
        return getJsonType() == JsonType.NUMBER
                ? IJsonNumber.class.cast(this)
                : null;
    }

    public default IJsonObject asJsonObject() {
        return getJsonType() == JsonType.OBJECT
                ? IJsonObject.class.cast(this)
                : null;
    }

    public default IJsonString asJsonString() {
        return getJsonType() == JsonType.STRING
                ? IJsonString.class.cast(this)
                : null;
    }

    public JsonType getJsonType();
}
