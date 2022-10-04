package pd.codec.json;

public final class JsonCodec {

    private static final IJsonFactory factory = new SimpleJsonFactory();

    public static <T> T convertToJava(IJson json, Class<T> targetClass, JsonTypeConfig config) {
        if (config == null) {
            config = new JsonTypeConfig();
        }
        return new JsonInverter(config).convertToJava(json, targetClass);
    }

    public static IJson convertToJson(Object object) {
        return new JsonConverter(factory).convertToJson(object);
    }

    public static <T> T decode(String jsonText, Class<T> targetClass, JsonTypeConfig config) {
        IJson json = deserialize(jsonText);
        return convertToJava(json, targetClass, config);
    }

    public static IJson deserialize(String jsonText) {
        return new JsonDeserializer(factory).deserialize(jsonText);
    }

    public static String encode(Object object, JsonFormatConfig config) {
        IJson json = convertToJson(object);
        return serialize(json, config);
    }

    public static String serialize(IJson json, JsonFormatConfig config) {
        if (config == null) {
            config = JsonFormatConfig.cheetsheetStyle();
        }
        return new JsonSerializer(config).serialize(json);
    }

    private JsonCodec() {
        // private dummy
    }
}
