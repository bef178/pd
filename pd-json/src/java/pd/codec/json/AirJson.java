package pd.codec.json;

import lombok.Getter;
import lombok.Setter;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.deserializer.Deserializer;
import pd.codec.json.deserializer.json2java.MappingToObjectConfig;
import pd.codec.json.deserializer.json2java.MappingToObjectExecutor;
import pd.codec.json.serializer.Serializer;
import pd.codec.json.serializer.SerializingConfig;
import pd.codec.json.serializer.java2json.MappingToJsonConfig;
import pd.codec.json.serializer.java2json.MappingToJsonExecutor;

public class AirJson {

    private static final AirJson one = new AirJson();

    public static AirJson defaultInstance() {
        return one;
    }

    @Getter
    @Setter
    private JsonFactory jsonFactory = JsonFactory.getFactory();

    private final SerializingConfig serializingConfig = new SerializingConfig();

    private final MappingToJsonConfig mappingToJsonConfig = new MappingToJsonConfig();

    private final MappingToObjectConfig mappingToObjectConfig = new MappingToObjectConfig();

    public AirJson configSerializingStyle(SerializingConfig.Style style) {
        serializingConfig.loadStyle(style);
        return this;
    }

    public String serialize(Object o) {
        return new Serializer(serializingConfig).serialize(mapToJson(o));
    }

    public Json mapToJson(Object o) {
        return new MappingToJsonExecutor(jsonFactory, mappingToJsonConfig).convert(o);
    }

    public <T> T deserialize(String s, Class<T> targetClass) {
        return mapToObject(deserialize(s), targetClass);
    }

    public Json deserialize(String s) {
        return new Deserializer(jsonFactory).deserialize(s);
    }

    public <T> T mapToObject(Json json, Class<T> targetClass) {
        return new MappingToObjectExecutor(mappingToObjectConfig).convert(json, targetClass);
    }
}
