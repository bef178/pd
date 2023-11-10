package pd.codec.json.datatype;

public interface JsonBoolean extends Json {

    boolean getBoolean();

    @Override
    default JsonType getJsonType() {
        return JsonType.BOOLEAN;
    }

    JsonBoolean set(boolean value);
}
