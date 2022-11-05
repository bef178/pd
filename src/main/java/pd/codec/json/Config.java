package pd.codec.json;

import java.util.LinkedHashMap;

class Config {

    public final IJsonFactory f;

    public JsonFormatConfig formatConfig = JsonFormatConfig.cheatsheetStyle();

    public JsonTypeConfig typeConfig = new JsonTypeConfig();

    public LinkedHashMap<Class<?>, IFuncConvertToJson> encoders = new LinkedHashMap<>();

    public LinkedHashMap<Class<?>, IFuncConvertToObject> decoders = new LinkedHashMap<>();

    public Config(IJsonFactory f) {
        this.f = f;
    }

    public <T> IFuncConvertToJson<T> registerEncoder(Class<T> surfacedClass, IFuncConvertToJson<T> func) {
        if (func == null) {
            throw new NullPointerException();
        }
        return encoders.put(surfacedClass, func);
    }

    public <T> IFuncConvertToObject<T> registerDecoder(Class<T> surfacedClass, IFuncConvertToObject<T> func) {
        if (func == null) {
            throw new NullPointerException();
        }
        return decoders.put(surfacedClass, func);
    }
}
