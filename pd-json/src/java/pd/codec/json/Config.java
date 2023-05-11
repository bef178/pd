package pd.codec.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.datafactory.IJsonFactory;
import pd.codec.json.mapper.json2javaobject.JsonToJavaObjectConfig;
import pd.codec.json.mapper.javaobject2json.JavaObjectToJsonConfig;
import pd.codec.json.parser.SerializationConfig;

class Config {

    public final IJsonFactory f;

    public final SerializationConfig serializationConfig = new SerializationConfig();

    public final JsonToJavaObjectConfig jsonToJavaObjectConfig;

    public final JavaObjectToJsonConfig javaObjectToJsonConfig;

    public Config(IJsonFactory factory) {
        this.f = factory;

        jsonToJavaObjectConfig = new JsonToJavaObjectConfig();
        jsonToJavaObjectConfig.register(List.class, ArrayList.class);
        jsonToJavaObjectConfig.register(Map.class, LinkedHashMap.class);

        javaObjectToJsonConfig = new JavaObjectToJsonConfig();
    }
}
