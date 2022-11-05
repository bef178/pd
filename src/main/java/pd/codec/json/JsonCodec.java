package pd.codec.json;

import static pd.fenc.CurvePattern.format;

public final class JsonCodec {

    public static final IJsonFactory f = new SimpleJsonFactory();

    private static final JsonCodec one;

    static {
        one = new JsonCodec(false);
    }

    public static JsonCodec singleton() {
        return one;
    }

    private final Config config = new Config(f);

    private final boolean configurable;

    public JsonCodec() {
        this(true);
    }

    private JsonCodec(boolean configurable) {
        this.configurable = configurable;
    }

    public JsonCodec configEncoder(SerializationStyle option) {
        if (!configurable) {
            throw new RuntimeException("not configurable");
        }
        switch (option) {
            case CHEATSHEET:
                config.formatConfig.mountCheatsheetStyle();
                break;
            case MULTILINES:
                config.formatConfig.mountMultilinesStyle();
                break;
            default:
                throw new RuntimeException(format("unknown option `{}`", option.name()));
        }
        return this;
    }

    public JsonCodec configEncoder(SerializationOption option, String value) {
        if (!configurable) {
            throw new RuntimeException("not configurable");
        }
        if (value == null) {
            throw new NullPointerException("value should not be null");
        }
        switch (option) {
            case MARGIN:
                config.formatConfig.margin = value;
                break;
            case INDENT:
                config.formatConfig.indent = value;
                break;
            case EOL:
                config.formatConfig.eol = value;
                break;
            case COLON_PREFIX:
                config.formatConfig.colonPrefix = value;
                break;
            case COLON_SUFFIX:
                config.formatConfig.colonSuffix = value;
                break;
            default:
                throw new RuntimeException(format("unknown option `{}`", option.name()));
        }
        return this;
    }

    public <T> JsonCodec configEncoder(Class<T> surfacedClass, IFuncConvertToJson<T> func) {
        if (!configurable) {
            throw new RuntimeException("not configurable");
        }
        config.registerEncoder(surfacedClass, func);
        return this;
    }

    public <T> JsonCodec configDecoder(Class<T> surfacedClass, IFuncConvertToObject<T> func) {
        if (!configurable) {
            throw new RuntimeException("not configurable");
        }
        config.registerDecoder(surfacedClass, func);
        return this;
    }

    public String serialize(IJson json) {
        return new JsonSerializer(config.formatConfig).serialize(json);
    }

    public IJson deserialize(String s) {
        return new JsonDeserializer(f).deserialize(s);
    }

    public IJson convertToJson(Object object) {
        return new ConverterToJson(config).convertToJson(object);
    }

    public <T> T convertToObject(IJson json, Class<T> surfacedClass) {
        return new ConverterToObject(config).convertToObject(json, surfacedClass);
    }

    public String encode(Object object) {
        IJson json = convertToJson(object);
        return serialize(json);
    }

    public <T> T decode(String s, Class<T> surfacedClass) {
        IJson json = deserialize(s);
        return convertToObject(json, surfacedClass);
    }
}
