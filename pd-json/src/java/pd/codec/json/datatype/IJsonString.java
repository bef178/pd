package pd.codec.json.datatype;

import pd.codec.json.JsonType;

public interface IJsonString extends IJson {

    @Override
    default JsonType getJsonType() {
        return JsonType.STRING;
    }

    String getString();

    IJsonString set(String value);
}
