package pd.codec.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.json2object.TypeMapping;
import pd.codec.json.object2json.JsonMapping;

class Config {

    public final IJsonFactory f;

    public JsonFormatConfig formatConfig = JsonFormatConfig.cheatsheetStyle();

    public final TypeMapping typeMapping;

    public final JsonMapping jsonMapping;

    public Config(IJsonFactory f) {
        this.f = f;

        typeMapping = new TypeMapping();
        typeMapping.register(List.class, ArrayList.class);
        typeMapping.register(Map.class, LinkedHashMap.class);

        jsonMapping = new JsonMapping();
    }
}
