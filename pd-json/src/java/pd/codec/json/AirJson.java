package pd.codec.json;

import lombok.Getter;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.deserializer.Deserializer;
import pd.codec.json.generalizer.Generalizer;
import pd.codec.json.generalizer.GeneralizingConfig;
import pd.codec.json.serializer.Serializer;
import pd.codec.json.serializer.SerializingConfig;
import pd.codec.json.specializer.Specializer;
import pd.codec.json.specializer.SpecializingConfig;

public class AirJson {

    private static final AirJson one = new AirJson();

    public static AirJson defaultInstance() {
        return one;
    }

    @Getter
    private final JsonFactory jsonFactory;

    private final SerializingConfig serializingConfig = new SerializingConfig();

    private final GeneralizingConfig generalizingConfig = new GeneralizingConfig();

    private final SpecializingConfig specializingConfig = new SpecializingConfig();

    public AirJson() {
        this(JsonFactory.getFactory());
    }

    public AirJson(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    public AirJson configSerializingStyle(SerializingConfig.Style style) {
        serializingConfig.loadStyle(style);
        return this;
    }

    public String serialize(Object o) {
        return new Serializer(serializingConfig).serialize(mapToJson(o));
    }

    public Json mapToJson(Object o) {
        return new Generalizer(jsonFactory, generalizingConfig).convert(o);
    }

    public <T> T deserialize(String s, Class<T> targetClass) {
        return mapToObject(deserialize(s), targetClass);
    }

    public Json deserialize(String s) {
        return new Deserializer(jsonFactory).deserialize(s);
    }

    public <T> T mapToObject(Json json, Class<T> targetClass) {
        return new Specializer(specializingConfig).convert(json, targetClass);
    }
}
