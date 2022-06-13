package pd.json.type;

public interface IJsonString extends IJson {

    @Override
    public default JsonType getJsonType() {
        return JsonType.STRING;
    }

    public String getString();

    public IJsonString set(String value);
}
