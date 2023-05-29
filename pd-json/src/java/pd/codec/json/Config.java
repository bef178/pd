package pd.codec.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.mapper.java2json.JavaToJsonConfig;
import pd.codec.json.mapper.json2java.JsonToJavaConfig;
import pd.codec.json.parser.SerializationConfig;

class Config {

    public final JsonFactory f;

    public final SerializationConfig serializationConfig = new SerializationConfig();

    public final JsonToJavaConfig jsonToJavaConfig;

    public final JavaToJsonConfig javaToJsonConfig;

    public Config(JsonFactory factory) {
        this.f = factory;

        jsonToJavaConfig = new JsonToJavaConfig();
        jsonToJavaConfig.register(List.class, ArrayList.class);
        jsonToJavaConfig.register(Map.class, LinkedHashMap.class);

        javaToJsonConfig = new JavaToJsonConfig();
    }
}
