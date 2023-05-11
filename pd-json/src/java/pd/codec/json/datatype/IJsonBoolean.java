package pd.codec.json.datatype;

import pd.codec.json.JsonType;

public interface IJsonBoolean extends IJson {

    boolean getBoolean();

    @Override
    default JsonType getJsonType() {
        return JsonType.BOOLEAN;
    }

    IJsonBoolean set(boolean value);
}
