package pd.codec.json.datatype;

public interface JsonString extends Json {

    @Override
    default JsonType getJsonType() {
        return JsonType.STRING;
    }

    String getString();

    JsonString set(String value);
}
