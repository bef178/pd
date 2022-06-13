package pd.json;

import pd.json.simplejson.SimpleJsonFactory;
import pd.json.type.IJson;

public class JsonCodec {

    public static final IJsonFactory factory = new SimpleJsonFactory();

    private static final JsonSerializer serializer = new JsonSerializer(factory);

    private static final JsonDeserializer deserializer = new JsonDeserializer(factory);

    public static <T> T deserialize(String jsonCode, Class<T> expectedJavaClass) {
        IJson json = deserializer.deserialize(jsonCode);
        if (IJson.class.isAssignableFrom(expectedJavaClass)) {
            return expectedJavaClass.cast(json);
        }
        return deserializer.convert(json, expectedJavaClass);
    }

    public static String serialize(Object object) {
        return serialize(object, "", "", "", 0);
    }

    public static String serialize(Object object, String margin, String indent, String eol, int numIndents) {
        IJson json = IJson.class.isAssignableFrom(object.getClass())
                ? IJson.class.cast(object)
                : serializer.convert(object);
        return serializer.serialize(json, margin, indent, eol, numIndents);
    }
}
