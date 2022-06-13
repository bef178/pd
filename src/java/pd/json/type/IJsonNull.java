package pd.json.type;

public interface IJsonNull extends IJson {

    @Override
    public default JsonType getJsonType() {
        return JsonType.NULL;
    }

    public default Object getNull() {
        return null;
    }
}
