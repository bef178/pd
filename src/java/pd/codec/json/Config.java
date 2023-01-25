package pd.codec.json;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.json.json2object.TypeMapping;

class Config {

    public final IJsonFactory f;

    public JsonFormatConfig formatConfig = JsonFormatConfig.cheatsheetStyle();

    public final TypeMapping typeMapping;

    public LinkedHashMap<Class<?>, IFuncConvertToJson> encoders = new LinkedHashMap<>();

    public Config(IJsonFactory f) {
        this.f = f;
        typeMapping = new TypeMapping();
        typeMapping.register(List.class, ArrayList.class);
        typeMapping.register(Map.class, LinkedHashMap.class);
    }

    public <T> IFuncConvertToJson<T> registerEncoder(Class<T> surfacedClass, IFuncConvertToJson<T> func) {
        if (func == null) {
            throw new NullPointerException();
        }
        return encoders.put(surfacedClass, func);
    }
}
