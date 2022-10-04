package pd.codec.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PrimitiveIterator.OfInt;

import pd.codec.HexCodec;
import pd.fenc.ParsingException;
import pd.util.Cascii;

class JsonSerializer {

    static class Config {

        public static Config cheetsheetStyle() {
            Config config = new Config();
            config.margin = "";
            config.indent = "";
            config.eol = "";
            config.colonPrefix = "";
            config.colonSuffix = "";
            return config;
        }

        public static Config multilinesSytle() {
            Config config = new Config();
            config.margin = "";
            config.indent = "    ";
            config.eol = "\n";
            config.colonPrefix = "";
            config.colonSuffix = " ";
            return config;
        }

        String margin = "";
        String indent = "";
        String eol = "";
        String colonPrefix = "";
        String colonSuffix = "";
    }

    private final IJsonFactory factory;

    public JsonSerializer(IJsonFactory factory) {
        this.factory = factory;
    }

    private IJson convert(Field field, Object o) throws IllegalArgumentException, IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            if (fieldType == boolean.class) {
                return factory.createJsonBoolean(field.getBoolean(o));
            }

            if (fieldType == byte.class) {
                return factory.createJsonNumber(field.getByte(o));
            } else if (fieldType == char.class) {
                return factory.createJsonNumber(field.getChar(o));
            } else if (fieldType == short.class) {
                return factory.createJsonNumber(field.getShort(o));
            } else if (fieldType == int.class) {
                return factory.createJsonNumber(field.getInt(o));
            } else if (fieldType == long.class) {
                return factory.createJsonNumber(field.getLong(o));
            }

            if (fieldType == float.class) {
                return factory.createJsonNumber(field.getFloat(o));
            } else if (fieldType == double.class) {
                return factory.createJsonNumber(field.getDouble(o));
            }

            throw new ParsingException();
        }
        return convert(field.get(o));
    }

    /**
     * `Object` => `IJson`<br/>
     * serialize public fields only
     */
    @SuppressWarnings("unchecked")
    public IJson convert(Object o) {
        if (o != null && IJson.class.isAssignableFrom(o.getClass())) {
            return IJson.class.cast(o);
        }

        if (o == null) {
            return factory.getJsonNull();
        }

        if (o instanceof Boolean) {
            return factory.createJsonBoolean((Boolean) o);
        }

        if (o instanceof Byte) {
            return factory.createJsonNumber((Byte) o);
        } else if (o instanceof Character) {
            return factory.createJsonNumber((Character) o);
        } else if (o instanceof Short) {
            return factory.createJsonNumber((Short) o);
        } else if (o instanceof Integer) {
            return factory.createJsonNumber((Integer) o);
        } else if (o instanceof Long) {
            return factory.createJsonNumber((Long) o);
        }

        if (o instanceof Float) {
            return factory.createJsonNumber((Float) o);
        } else if (o instanceof Double) {
            return factory.createJsonNumber((Double) o);
        }

        if (o instanceof String) {
            return factory.createJsonString((String) o);
        }

        if (o instanceof List) {
            IJsonArray a = factory.createJsonArray();
            for (Object element : (List<Object>) o) {
                a.add(convert(element));
            }
            return a;
        } else if (o.getClass().isArray()) {
            IJsonArray a = factory.createJsonArray();
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(o, i);
                a.add(convert(element));
            }
            return a;
        }

        if (o instanceof Map) {
            IJsonObject jsonObject = factory.createJsonObject();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
                String key = entry.getKey().toString();
                jsonObject.put(key, convert(entry.getValue()));
            }
            return jsonObject;
        } else {
            IJsonObject jsonObject = factory.createJsonObject();
            // public fields only
            for (Field field : o.getClass().getFields()) {
                String key = field.getName();
                try {
                    jsonObject.put(key, convert(field, o));
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ParsingException(e);
                }
            }
            return jsonObject;
        }
    }

    /**
     * `IJson` => `String`<br/>
     */
    public String serialize(IJson json, Config config) {
        StringBuilder sb = new StringBuilder();
        serializeJson(json, config, 0, sb);
        return sb.toString();
    }

    private void serializeJson(IJson json, Config config, int numIndents, StringBuilder sb) {
        if (json instanceof IJsonNull) {
            sb.append("null");
        } else if (json instanceof IJsonBoolean) {
            sb.append(Boolean.toString(IJsonBoolean.class.cast(json).getBoolean()));
        } else if (json instanceof IJsonNumber) {
            sb.append(IJsonNumber.class.cast(json).toString());
        } else if (json instanceof IJsonString) {
            serializeJsonString(IJsonString.class.cast(json).getString(), sb);
        } else if (json instanceof IJsonArray) {
            serializeJsonArray(IJsonArray.class.cast(json), config, numIndents, sb);
        } else if (json instanceof IJsonObject) {
            serializeJsonObject(IJsonObject.class.cast(json), config, numIndents, sb);
        } else {
            throw new ParsingException();
        }
    }

    private void serializeJsonArray(IJsonArray jsonArray, Config config, int numIndents,
            StringBuilder sb) {

        sb.append('[');

        if (!jsonArray.isEmpty()) {
            sb.append(config.eol);
        }

        numIndents++;

        Iterator<IJson> it = jsonArray.iterator();
        while (it.hasNext()) {
            serializeMarginAndIndents(config, numIndents, sb);
            serializeJson(it.next(), config, numIndents, sb);
            if (it.hasNext()) {
                sb.append(',');
            }
            sb.append(config.eol);
        }

        numIndents--;

        if (!jsonArray.isEmpty()) {
            serializeMarginAndIndents(config, numIndents, sb);
        }

        sb.append(']');
    }

    private void serializeJsonObject(IJsonObject jsonObject, Config config, int numIndents,
            StringBuilder sb) {

        sb.append('{');

        if (!jsonObject.isEmpty()) {
            sb.append(config.eol);
        }

        numIndents++;

        Iterator<Entry<String, IJson>> it = jsonObject.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, IJson> entry = it.next();
            String key = entry.getKey();
            IJson value = entry.getValue();

            serializeMarginAndIndents(config, numIndents, sb);

            serializeJsonString(key, sb);
            sb.append(config.colonPrefix);
            sb.append(':');
            sb.append(config.colonSuffix);
            serializeJson(value, config, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(config.eol);
        }

        numIndents--;

        if (!jsonObject.isEmpty()) {
            serializeMarginAndIndents(config, numIndents, sb);
        }

        sb.append('}');
    }

    private void serializeJsonString(String s, StringBuilder sb) {
        sb.appendCodePoint('\"');
        OfInt it = s.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            switch (ch) {
                case '\"':
                case '\\':
                    sb.append('\\').append(ch);
                    break;
                case '\b':
                    sb.append('\\').append('b');
                    break;
                case '\f':
                    sb.append('\\').append('f');
                    break;
                case '\n':
                    sb.append('\\').append('n');
                    break;
                case '\r':
                    sb.append('\\').append('r');
                    break;
                case '\t':
                    sb.append('\\').append('t');
                    break;
                default:
                    if (Cascii.isControl(ch)) {
                        int[] a = new int[2];
                        HexCodec.encode1byte((byte) ch, a, 0);
                        sb.append('\\').append('u').append('0').append('0').append((char) a[0]).append((char) a[1]);
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
            }
        }
        sb.appendCodePoint('\"');
    }

    private void serializeMarginAndIndents(Config config, int numIndents, StringBuilder sb) {
        sb.append(config.margin);
        for (int i = 0; i < numIndents; i++) {
            sb.append(config.indent);
        }
    }
}
