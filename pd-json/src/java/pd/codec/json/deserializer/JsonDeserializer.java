package pd.codec.json.deserializer;

import lombok.Getter;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.deserializer.json2java.DeserializeJsonConfig;
import pd.codec.json.deserializer.json2java.DeserializeJsonExecutor;

public class JsonDeserializer {

    @Getter
    private final JsonFactory jsonFactory;

    @Getter
    private final DeserializeJsonConfig deserializeJsonConfig;

    public JsonDeserializer() {
        this(JsonFactory.getFactory(), new DeserializeJsonConfig());
    }

    public JsonDeserializer(JsonFactory jsonFactory, DeserializeJsonConfig deserializeJsonConfig) {
        this.jsonFactory = jsonFactory;
        this.deserializeJsonConfig = deserializeJsonConfig;
    }

    public <T> T deserialize(String s, Class<T> targetClass) {
        Json json = deserializeToJson(s);
        return deserializeJson(json, targetClass);
    }

    public Json deserializeToJson(String s) {
        return new DeserializeToJsonExecutor(jsonFactory).deserialize(s);
    }

    public <T> T deserializeJson(Json json, Class<T> targetClass) {
        return new DeserializeJsonExecutor(deserializeJsonConfig).convert(json, targetClass);
    }
}
