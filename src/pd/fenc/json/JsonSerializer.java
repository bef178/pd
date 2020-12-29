package pd.fenc.json;

public class JsonSerializer {

    static final JsonDeserializer deserializer = new JsonDeserializer();

    public static final IJsonCreator creator = deserializer.creator;

    public static IJsonValue deserialize(String jsonCode) {
        return deserializer.deserialize(jsonCode);
    }

    public static <T extends IJsonValue> T deserialize(String jsonCode, Class<T> expectedClass) {
        return deserializer.deserialize(jsonCode, expectedClass);
    }

    public static String serialize(IJsonValue value) {
        return value.serialize();
    }

    public static String serialize(IJsonValue value, String margin, String indent, String eol,
            int numIndents) {
        StringBuilder sb = new StringBuilder();
        serializeJsonValue(value, margin, indent, eol, numIndents, sb);
        return sb.toString();
    }

    static void serializeJsonValue(IJsonValue value, String margin, String indent, String eol,
            int numIndents, StringBuilder sb) {
        if (value instanceof IJsonObject) {
            sb.append(((IJsonObject) value).serialize(margin, indent, eol, numIndents));
        } else if (value instanceof IJsonArray) {
            sb.append(((IJsonArray) value).serialize(margin, indent, eol, numIndents));
        } else {
            sb.append(value.serialize());
        }
    }

    static void serializeMarginAndIndents(String margin, String indent, int numIndents,
            StringBuilder sb) {
        sb.append(margin);
        for (int i = 0; i < numIndents; i++) {
            sb.append(indent);
        }
    }
}
