package pd.codec.json;

import pd.codec.json.datafactory.IJsonFactory;
import pd.codec.json.datatype.IJson;
import pd.codec.json.mapper.javaobject2json.IMapToJson;
import pd.codec.json.mapper.json2javaobject.IMapToJavaType;
import pd.codec.json.mapper.json2javaobject.JsonToJavaObjectConverter;
import pd.codec.json.mapper.javaobject2json.JavaObjectToJsonConverter;
import pd.codec.json.parser.JsonDeserializer;
import pd.codec.json.parser.JsonSerializer;
import pd.codec.json.parser.SerializationConfig;

public final class JsonCodec {

    public static final IJsonFactory f = IJsonFactory.getFactory();

    private static final JsonCodec one = new JsonCodec().freeze();

    public static JsonCodec singleton() {
        return one;
    }

    private final Config config = new Config(f);

    private boolean configFrozen = false;

    public <T> JsonCodec config(Class<T> declaredClass, Class<? extends T> actualClass) {
        if (configFrozen) {
            throw new RuntimeException("not configurable");
        }
        config.jsonToJavaObjectConfig.register(declaredClass, actualClass);
        return this;
    }

    public <T> JsonCodec config(Class<T> declaredClass, IMapToJavaType<T> mapper) {
        if (configFrozen) {
            throw new RuntimeException("not configurable");
        }
        config.jsonToJavaObjectConfig.register(declaredClass, mapper);
        return this;
    }

    public JsonCodec config(Class<?> declaredClass, IMapToJson mapper) {
        if (configFrozen) {
            throw new RuntimeException("not configurable");
        }
        config.javaObjectToJsonConfig.register(declaredClass, mapper);
        return this;
    }

    public JsonCodec config(SerializationConfig.Style style) {
        if (configFrozen) {
            throw new RuntimeException("not configurable");
        }
        config.serializationConfig.init(style);
        return this;
    }

    public JsonCodec config(SerializationConfig.Option option, String value) {
        if (configFrozen) {
            throw new RuntimeException("not configurable");
        }
        if (value == null) {
            throw new NullPointerException("value should not be null");
        }
        config.serializationConfig.setOption(option, value);
        return this;
    }

    public JsonCodec freeze() {
        configFrozen = true;
        return this;
    }

    public String serialize(IJson json) {
        return new JsonSerializer(config.serializationConfig).serialize(json);
    }

    public IJson deserialize(String s) {
        return new JsonDeserializer(config.f).deserialize(s);
    }

    public IJson convertToJson(Object object) {
        return new JavaObjectToJsonConverter(config.f, config.javaObjectToJsonConfig).convert(object);
    }

    public <T> T convertToJavaObject(IJson json, Class<T> targetClass) {
        return new JsonToJavaObjectConverter(config.jsonToJavaObjectConfig).convert(json, targetClass);
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
