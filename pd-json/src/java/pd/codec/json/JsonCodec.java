package pd.codec.json;

import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.mapper.java2json.MapToJsonInstance;
import pd.codec.json.mapper.json2java.MapToJavaType;
import pd.codec.json.mapper.json2java.JsonToJavaConverter;
import pd.codec.json.mapper.java2json.JavaToJsonConverter;
import pd.codec.json.parser.JsonDeserializer;
import pd.codec.json.parser.JsonSerializer;
import pd.codec.json.parser.SerializationConfig;

public final class JsonCodec {

    public static final JsonFactory f = JsonFactory.getFactory();

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
        config.jsonToJavaConfig.register(declaredClass, actualClass);
        return this;
    }

    public <T> JsonCodec config(Class<T> declaredClass, MapToJavaType<T> mapper) {
        if (configFrozen) {
            throw new RuntimeException("not configurable");
        }
        config.jsonToJavaConfig.register(declaredClass, mapper);
        return this;
    }

    public JsonCodec config(Class<?> declaredClass, MapToJsonInstance mapper) {
        if (configFrozen) {
            throw new RuntimeException("not configurable");
        }
        config.javaToJsonConfig.register(declaredClass, mapper);
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

    public String serialize(Json json) {
        return new JsonSerializer(config.serializationConfig).serialize(json);
    }

    public Json deserialize(String s) {
        return new JsonDeserializer(config.f).deserialize(s);
    }

    public Json convertToJsonInstance(Object object) {
        return new JavaToJsonConverter(config.f, config.javaToJsonConfig).convert(object);
    }

    public <T> T convertToJavaInstance(Json json, Class<T> targetClass) {
        return new JsonToJavaConverter(config.jsonToJavaConfig).convert(json, targetClass);
    }

    public String encode(Object object) {
        Json json = convertToJsonInstance(object);
        return serialize(json);
    }

    public <T> T decode(String s, Class<T> surfacedClass) {
        Json json = deserialize(s);
        return convertToJavaInstance(json, surfacedClass);
    }
}
