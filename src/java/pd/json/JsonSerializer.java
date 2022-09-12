package pd.json;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PrimitiveIterator.OfInt;

import pd.codec.HexCodec;
import pd.json.type.IJson;
import pd.json.type.IJsonArray;
import pd.json.type.IJsonBoolean;
import pd.json.type.IJsonNull;
import pd.json.type.IJsonNumber;
import pd.json.type.IJsonObject;
import pd.json.type.IJsonString;
import pd.util.Cascii;

class JsonSerializer {

    private final IJsonFactory factory;

    public JsonSerializer(IJsonFactory factory) {
        this.factory = factory;
    }

    private IJson convert(Field field, Object o) throws IllegalArgumentException, IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            if (fieldType == boolean.class) {
                return factory.newJsonBoolean(field.getBoolean(o));
            }

            if (fieldType == byte.class) {
                return factory.newJsonNumber().set(field.getByte(o));
            } else if (fieldType == char.class) {
                return factory.newJsonNumber().set(field.getChar(o));
            } else if (fieldType == short.class) {
                return factory.newJsonNumber().set(field.getShort(o));
            } else if (fieldType == int.class) {
                return factory.newJsonNumber().set(field.getInt(o));
            } else if (fieldType == long.class) {
                return factory.newJsonNumber().set(field.getLong(o));
            }

            if (fieldType == float.class) {
                return factory.newJsonNumber().set(field.getFloat(o));
            } else if (fieldType == double.class) {
                return factory.newJsonNumber().set(field.getDouble(o));
            }

            throw new JsonException();
        }
        return convert(field.get(o));
    }

    /**
     * `Object` => `IJson`<br/>
     * serialize public fields only
     */
    @SuppressWarnings("unchecked")
    public IJson convert(Object o) {
        if (o == null) {
            return factory.newJsonNull();
        }

        if (o instanceof Boolean) {
            return factory.newJsonBoolean((Boolean) o);
        }

        if (o instanceof Byte) {
            return factory.newJsonNumber().set((Byte) o);
        } else if (o instanceof Character) {
            return factory.newJsonNumber().set((Character) o);
        } else if (o instanceof Short) {
            return factory.newJsonNumber().set((Short) o);
        } else if (o instanceof Integer) {
            return factory.newJsonNumber().set((Integer) o);
        } else if (o instanceof Long) {
            return factory.newJsonNumber().set((Long) o);
        }

        if (o instanceof Float) {
            return factory.newJsonNumber().set((Float) o);
        } else if (o instanceof Double) {
            return factory.newJsonNumber().set((Double) o);
        }

        if (o instanceof String) {
            return factory.newJsonString().set((String) o);
        }

        if (o instanceof List) {
            IJsonArray a = factory.newJsonArray();
            for (Object element : (List<Object>) o) {
                IJson json = convert(element);
                a.add(json);
            }
            return a;
        } else if (o.getClass().isArray()) {
            IJsonArray a = factory.newJsonArray();
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(o, i);
                IJson json = convert(element);
                a.add(json);
            }
            return a;
        }

        if (o instanceof Map) {
            IJsonObject jsonObject = factory.newJsonObject();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
                String key = entry.getKey().toString();
                IJson json = convert(entry.getValue());
                jsonObject.put(key, json);
            }
            return jsonObject;
        } else {
            IJsonObject jsonObject = factory.newJsonObject();
            for (Field field : o.getClass().getFields()) {
                String key = field.getName();
                try {
                    IJson json = convert(field, o);
                    jsonObject.put(key, json);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new JsonException(e);
                }
            }
            return jsonObject;
        }
    }

    /**
     * `IJson` => `String`<br/>
     */
    public String serialize(IJson json, String margin, String indent, String eol, int numIndents) {
        StringBuilder sb = new StringBuilder();
        serializeJson(json, margin, indent, eol, numIndents, sb);
        return sb.toString();
    }

    private void serializeJson(IJson json, String margin, String indent, String eol, int numIndents, StringBuilder sb) {
        if (json instanceof IJsonNull) {
            sb.append("null");
        } else if (json instanceof IJsonBoolean) {
            sb.append(Boolean.toString(IJsonBoolean.class.cast(json).getBoolean()));
        } else if (json instanceof IJsonNumber) {
            sb.append(IJsonNumber.class.cast(json).toString());
        } else if (json instanceof IJsonString) {
            serializeJsonString(IJsonString.class.cast(json).getString(), sb);
        } else if (json instanceof IJsonArray) {
            serializeJsonArray(IJsonArray.class.cast(json), margin, indent, eol, numIndents, sb);
        } else if (json instanceof IJsonObject) {
            serializeJsonObject(IJsonObject.class.cast(json), margin, indent, eol, numIndents, sb);
        } else {
            throw new JsonException();
        }
    }

    private void serializeJsonArray(IJsonArray jsonArray, String margin, String indent, String eol, int numIndents,
            StringBuilder sb) {

        if (margin == null) {
            margin = "";
        }

        if (indent == null) {
            indent = "";
        }

        if (eol == null) {
            eol = "";
        }

        sb.append('[');

        if (!jsonArray.isEmpty()) {
            sb.append(eol);
        }

        numIndents++;

        Iterator<IJson> it = jsonArray.iterator();
        while (it.hasNext()) {
            IJson json = it.next();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            serializeJson(json, margin, indent, eol, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(eol);
        }

        numIndents--;

        if (!jsonArray.isEmpty()) {
            serializeMarginAndIndents(margin, indent, numIndents, sb);
        }

        sb.append(']');
    }

    private void serializeJsonObject(IJsonObject jsonObject, String margin, String indent, String eol, int numIndents,
            StringBuilder sb) {

        if (margin == null) {
            margin = "";
        }

        if (indent == null) {
            indent = "";
        }

        if (eol == null) {
            eol = "";
        }

        sb.append('{');

        if (!jsonObject.isEmpty()) {
            sb.append(eol);
        }

        numIndents++;

        Iterator<Entry<String, IJson>> it = jsonObject.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, IJson> entry = it.next();
            String key = entry.getKey();
            IJson value = entry.getValue();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            serializeJsonString(key, sb);
            sb.append(':');
            serializeJson(value, margin, indent, eol, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(eol);
        }

        numIndents--;

        if (!jsonObject.isEmpty()) {
            serializeMarginAndIndents(margin, indent, numIndents, sb);
        }

        sb.append('}');
    }

    private void serializeJsonString(String s, StringBuilder sb) {
        sb.appendCodePoint('\"');
        OfInt it = s.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            if (ch == '\"' || ch == '\\') {
                sb.append('\\').append(ch);
            } else if (ch == '\b') {
                sb.append('\\').append('b');
            } else if (ch == '\f') {
                sb.append('\\').append('f');
            } else if (ch == '\n') {
                sb.append('\\').append('n');
            } else if (ch == '\r') {
                sb.append('\\').append('r');
            } else if (ch == '\t') {
                sb.append('\\').append('t');
            } else if (Cascii.isControl(ch)) {
                int[] a = new int[2];
                HexCodec.encode1byte((byte) ch, a, 0);
                sb.append('\\').append('u').append('0').append('0').append((char) a[0]).append((char) a[1]);
            } else {
                sb.appendCodePoint(ch);
            }
        }
        sb.appendCodePoint('\"');
    }

    private void serializeMarginAndIndents(String margin, String indent, int numIndents, StringBuilder sb) {
        sb.append(margin);
        for (int i = 0; i < numIndents; i++) {
            sb.append(indent);
        }
    }
}
