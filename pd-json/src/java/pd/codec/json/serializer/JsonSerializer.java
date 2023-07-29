package pd.codec.json.serializer;

import lombok.Getter;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.serializer.java2json.SerializeToJsonConfig;
import pd.codec.json.serializer.java2json.SerializeToJsonExecutor;

public class JsonSerializer {

    private final JsonFactory jsonFactory;

    @Getter
    private final SerializeToJsonConfig serializeToJsonConfig;

    @Getter
    private final SerializeJsonConfig serializeJsonConfig;

    public JsonSerializer() {
        this(JsonFactory.getFactory(), new SerializeToJsonConfig(), new SerializeJsonConfig());
    }

    public JsonSerializer(JsonFactory jsonFactory, SerializeToJsonConfig serializeToJsonConfig, SerializeJsonConfig serializeJsonConfig) {
        this.jsonFactory = jsonFactory;
        this.serializeToJsonConfig = serializeToJsonConfig;
        this.serializeJsonConfig = serializeJsonConfig;
    }

    public String serialize(Object object) {
        Json json = serializeToJson(object);
        return serializeJson(json);
    }

    public Json serializeToJson(Object object) {
        return new SerializeToJsonExecutor(jsonFactory, serializeToJsonConfig).convert(object);
    }

    public String serializeJson(Json json) {
        return new SerializeJsonExecutor(serializeJsonConfig).serialize(json);
    }
}
