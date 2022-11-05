package pd.codec.json;

public final class JsonCodec {

    public static final IJsonFactory f = new SimpleJsonFactory();

    private static final JsonCodec one;

    static {
        one = new JsonCodec();
    }

    public static JsonCodec singleton() {
        return one;
    }

    public final Config config = new Config();

    public String serialize(IJson json) {
        return new JsonSerializer(config.formatConfig).serialize(json);
    }

    public IJson deserialize(String s) {
        return new JsonDeserializer(f).deserialize(s);
    }

    public <T> T convertToJava(IJson json, Class<T> expectedClass) {
        return new JsonInverter(config.typeConfig).convertToJava(json, expectedClass);
    }

    public IJson convertToJson(Object object) {
        return new JsonConverter(f).convertToJson(object);
    }

    public String encode(Object object) {
        IJson json = convertToJson(object);
        return serialize(json);
    }

    public <T> T decode(String s, Class<T> expectedClass) {
        IJson json = deserialize(s);
        return convertToJava(json, expectedClass);
    }
}
