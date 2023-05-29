package pd.codec.json;

import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.mapper.java2json.JavaToJsonConverter;
import pd.codec.json.mapper.java2json.MapToJsonInstance;
import pd.codec.json.mapper.json2java.JsonToJavaConverter;
import pd.codec.json.mapper.json2java.MapToJavaType;
import pd.codec.json.parser.JsonDeserializer;
import pd.codec.json.parser.JsonSerializer;
import pd.codec.json.parser.SerializationConfig;

public final class JsonCodec {

    public static final JsonFactory f = JsonFactory.getFactory();

    private static final JsonCodec one = new JsonCodec().freeze();

    public static JsonCodec singleton() {
        return one;
    }

    private final Config config;

    public JsonCodec() {
        this(new Config());
    }

    public JsonCodec(Config config) {
        this.config = config;
    }

    public JsonCodec configureJavaToJsonMapper(Class<?> targetClass, MapToJsonInstance mapper) {
        config.configureToJsonMapper(targetClass, mapper);
        return this;
    }

    public <T> JsonCodec configureJsonToJavaMapper(Class<T> targetClass, Class<? extends T> actualClass) {
        config.configureJsonToJavaMapper(targetClass, actualClass);
        return this;
    }

    public <T> JsonCodec configureJsonToJavaMapper(Class<T> targetClass, MapToJavaType<T> mapper) {
        config.configureJsonToJavaMapper(targetClass, mapper);
        return this;
    }

    public JsonCodec configureParser(SerializationConfig.Style style) {
        config.configureParser(style);
        return this;
    }

    public JsonCodec configureParser(SerializationConfig.Option option, String value) {
        config.configureParser(option, value);
        return this;
    }

    public JsonCodec freeze() {
        config.freeze();
        return this;
    }

    public String serialize(Object object) {
        Json json = serializeToJson(object);
        return serializeJson(json);
    }

    public Json serializeToJson(Object object) {
        return new JavaToJsonConverter(config.f, config.javaToJsonConfig).convert(object);
    }

    public String serializeJson(Json json) {
        return new JsonSerializer(config.serializationConfig).serialize(json);
    }

    public <T> T deserialize(String s, Class<T> targetClass) {
        Json json = deserializeToJson(s);
        return deserializeJson(json, targetClass);
    }

    public Json deserializeToJson(String s) {
        return new JsonDeserializer(config.f).deserialize(s);
    }

    public <T> T deserializeJson(Json json, Class<T> targetClass) {
        return new JsonToJavaConverter(config.jsonToJavaConfig).convert(json, targetClass);
    }
}
