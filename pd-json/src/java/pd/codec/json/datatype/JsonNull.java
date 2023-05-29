package pd.codec.json.datatype;

import pd.codec.json.JsonType;

public interface JsonNull extends Json {

    @Override
    default JsonType getJsonType() {
        return JsonType.NULL;
    }

    default Object getNull() {
        return null;
    }
}
