package pd.json;

import lombok.Getter;
import pd.json.datafactory.JsonFactory;
import pd.json.datatype.Json;
import pd.json.deserializer.Deserializer;
import pd.json.generalizer.Generalizer;
import pd.json.generalizer.GeneralizingConfig;
import pd.json.serializer.Serializer;
import pd.json.serializer.SerializingConfig;
import pd.json.specializer.Specializer;
import pd.json.specializer.SpecializingConfig;

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
