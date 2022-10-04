package pd.codec.json;

import pd.codec.json.JsonSerializer.Config;

public class JsonCodec {

    private final JsonSerializer serializer;

    private final JsonDeserializer deserializer;

    public JsonCodec() {
        this(new SimpleJsonFactory());
    }

    public JsonCodec(IJsonFactory factory) {
        serializer = new JsonSerializer(factory);
        deserializer = new JsonDeserializer(factory);
    }

    public <T> T convert(IJson json, Class<T> expectedClass) {
        return deserializer.convert(json, expectedClass);
    }

    public IJson convert(Object object) {
        return serializer.convert(object);
    }

    public IJson deserialize(String jsonText) {
        return deserializer.deserialize(jsonText);
    }

    public String serialize(IJson json) {
        return serialize(json, Config.cheetsheetStyle());
    }

    public String serialize(IJson json, Config config) {
        return serializer.serialize(json, config);
    }
}
