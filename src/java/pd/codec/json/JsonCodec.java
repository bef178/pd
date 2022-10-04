package pd.codec.json;

public class JsonCodec {

    private final IJsonFactory factory;

    public JsonCodec() {
        this(new SimpleJsonFactory());
    }

    public JsonCodec(IJsonFactory factory) {
        this.factory = factory;
    }

    public <T> T convert(IJson json, Class<T> expectedClass) {
        return new JsonInverter().convertToJava(json, expectedClass);
    }

    public IJson convert(Object object) {
        return new JsonConverter(factory).convertToJson(object);
    }

    public IJson deserialize(String jsonText) {
        return new JsonDeserializer(factory).deserialize(jsonText);
    }

    public String serialize(IJson json) {
        return serialize(json, JsonFormatConfig.cheetsheetStyle());
    }

    public String serialize(IJson json, JsonFormatConfig config) {
        return new JsonSerializer(config).serialize(json);
    }
}
