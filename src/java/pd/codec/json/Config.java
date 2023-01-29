package pd.codec.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.json2object.TypeMapping;
import pd.codec.json.object2json.JsonMapping;
import pd.codec.json.serialization.SerializationConfig;

class Config {

    public final IJsonFactory f;

    public final SerializationConfig serializationConfig = new SerializationConfig();

    public final TypeMapping typeMapping;

    public final JsonMapping jsonMapping;

    public Config(IJsonFactory factory) {
        this.f = factory;

        typeMapping = new TypeMapping();
        typeMapping.register(List.class, ArrayList.class);
        typeMapping.register(Map.class, LinkedHashMap.class);

        jsonMapping = new JsonMapping();
    }
}
