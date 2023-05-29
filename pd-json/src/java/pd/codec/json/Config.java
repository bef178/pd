package pd.codec.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.mapper.java2json.JavaToJsonConfig;
import pd.codec.json.mapper.java2json.MapToJsonInstance;
import pd.codec.json.mapper.json2java.JsonToJavaConfig;
import pd.codec.json.mapper.json2java.MapToJavaType;
import pd.codec.json.parser.SerializationConfig;

class Config {

    public final JsonFactory f;

    public final SerializationConfig serializationConfig = new SerializationConfig();

    final JsonToJavaConfig jsonToJavaConfig;

    final JavaToJsonConfig javaToJsonConfig;

    private boolean froze;

    public Config() {
        this(JsonCodec.f);
    }

    public Config(JsonFactory factory) {
        this.f = factory;

        jsonToJavaConfig = new JsonToJavaConfig();
        jsonToJavaConfig.register(List.class, ArrayList.class);
        jsonToJavaConfig.register(Map.class, LinkedHashMap.class);

        javaToJsonConfig = new JavaToJsonConfig();
    }

    public Config freeze() {
        froze = true;
        return this;
    }

    private void checkFroze() {
        if (froze) {
            throw new RuntimeException("config frozen, not configurable");
        }
    }

    public <T> Config configureJsonToJavaMapper(Class<T> targetClass, MapToJavaType<T> mapper) {
        checkFroze();
        jsonToJavaConfig.register(targetClass, mapper);
        return this;
    }

    public <T> Config configureJsonToJavaMapper(Class<T> targetClass, Class<? extends T> actualClass) {
        checkFroze();
        jsonToJavaConfig.register(targetClass, actualClass);
        return this;
    }

    public Config configureToJsonMapper(Class<?> target, MapToJsonInstance mapper) {
        checkFroze();
        javaToJsonConfig.register(target, mapper);
        return this;
    }

    public Config configureParser(SerializationConfig.Style style) {
        checkFroze();
        serializationConfig.init(style);
        return this;
    }

    public Config configureParser(SerializationConfig.Option option, String value) {
        checkFroze();
        serializationConfig.setOption(option, value);
        return this;
    }
}
