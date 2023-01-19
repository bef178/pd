package pd.codec.json;

public interface IJsonBoolean extends IJson {

    public boolean getBoolean();

    @Override
    public default JsonType getJsonType() {
        return JsonType.BOOLEAN;
    }

    public IJsonBoolean set(boolean value);
}
