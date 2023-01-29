package pd.codec.json;

import pd.codec.json.json2object.JsonToObjectConverter;
import pd.codec.json.object2json.ObjectToJsonConverter;
import pd.codec.json.serialization.JsonDeserializer;
import pd.codec.json.serialization.JsonSerializer;
import pd.codec.json.serialization.SerializationConfig;

public final class JsonCodec {

    public static final IJsonFactory f = new SimpleJsonFactory();

    private static final JsonCodec one = new JsonCodec().freeze();

    public static JsonCodec singleton() {
        return one;
    }

    private final Config config = new Config(f);

    private boolean frozen = false;

    public JsonCodec configEncoder(SerializationConfig.Style style) {
        if (frozen) {
            throw new RuntimeException("not configurable");
        }
        config.serializationConfig.mountStyle(style);
        return this;
    }

    public JsonCodec configEncoder(SerializationConfig.Option option, String value) {
        if (frozen) {
            throw new RuntimeException("not configurable");
        }
        if (value == null) {
            throw new NullPointerException("value should not be null");
        }
        config.serializationConfig.setOption(option, value);
        return this;
    }

    public JsonCodec freeze() {
        frozen = true;
        return this;
    }

    public String serialize(IJson json) {
        return new JsonSerializer(config.serializationConfig).serialize(json);
    }

    public IJson deserialize(String s) {
        return new JsonDeserializer(config.f).deserialize(s);
    }

    public IJson convertToJson(Object object) {
        return new ObjectToJsonConverter(config.f, config.jsonMapping).convert(object);
    }

    public <T> T convertToJavaObject(IJson json, Class<T> targetClass) {
        return new JsonToObjectConverter(config.typeMapping).convert(json, targetClass);
    }

    public String encode(Object object) {
        IJson json = convertToJson(object);
        return serialize(json);
    }

    public <T> T decode(String s, Class<T> surfacedClass) {
        IJson json = deserialize(s);
        return convertToJavaObject(json, surfacedClass);
    }
}
