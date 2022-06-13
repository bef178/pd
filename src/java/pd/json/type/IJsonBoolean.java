package pd.json.type;

public interface IJsonBoolean extends IJson {

    public boolean getBoolean();

    @Override
    public default JsonType getJsonType() {
        return JsonType.BOOLEAN;
    }
}
