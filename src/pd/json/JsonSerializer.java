package pd.json;

import static pd.json.JsonCodec.tokenFactory;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pd.fenc.ParsingException;

class JsonSerializer {

    private IJsonToken serialize(Field field, Object o) throws IllegalArgumentException, IllegalAccessException {
        Class<?> fieldType = field.getType();
        if (fieldType.isPrimitive()) {
            if (fieldType == boolean.class) {
                return tokenFactory.newJsonBoolean(field.getBoolean(o));
            } else if (fieldType == byte.class) {
                return tokenFactory.newJsonInt(field.getByte(o));
            } else if (fieldType == char.class) {
                return tokenFactory.newJsonInt(field.getChar(o));
            } else if (fieldType == short.class) {
                return tokenFactory.newJsonInt(field.getShort(o));
            } else if (fieldType == int.class) {
                return tokenFactory.newJsonInt(field.getInt(o));
            } else if (fieldType == long.class) {
                return tokenFactory.newJsonInt(field.getLong(o));
            } else if (fieldType == float.class) {
                return tokenFactory.newJsonFloat(field.getFloat(o));
            } else if (fieldType == double.class) {
                return tokenFactory.newJsonFloat(field.getDouble(o));
            } else {
                throw new ParsingException();
            }
        }
        return serialize(field.get(o));
    }

    /**
     * `IJsonToken` => `String`<br/>
     */
    public String serialize(IJsonToken jsonToken, String margin, String indent, String eol, int numIndents) {
        StringBuilder sb = new StringBuilder();
        serializeJsonToken(jsonToken, margin, indent, eol, numIndents, sb);
        return sb.toString();
    }

    /**
     * `Object` => `IJsonToken`<br/>
     * serialize only public fields
     */
    @SuppressWarnings("unchecked")
    public IJsonToken serialize(Object o) {
        if (o == null) {
            return tokenFactory.newJsonNull();
        }

        if (o instanceof Boolean) {
            return tokenFactory.newJsonBoolean((Boolean) o);
        } else if (o instanceof Byte) {
            return tokenFactory.newJsonInt((Byte) o);
        } else if (o instanceof Character) {
            return tokenFactory.newJsonInt((Character) o);
        } else if (o instanceof Short) {
            return tokenFactory.newJsonInt((Short) o);
        } else if (o instanceof Integer) {
            return tokenFactory.newJsonInt((Integer) o);
        } else if (o instanceof Long) {
            return tokenFactory.newJsonInt((Long) o);
        } else if (o instanceof Float) {
            return tokenFactory.newJsonFloat((Float) o);
        } else if (o instanceof Double) {
            return tokenFactory.newJsonFloat((Double) o);
        }

        if (o instanceof String) {
            return tokenFactory.newJsonString((String) o);
        }

        if (o instanceof List) {
            IJsonArray token = tokenFactory.newJsonArray();
            for (Object element : (List<Object>) o) {
                IJsonToken value = serialize(element);
                token.add(value);
            }
            return token;
        } else if (o.getClass().isArray()) {
            IJsonArray token = tokenFactory.newJsonArray();
            int length = Array.getLength(o);
            for (int i = 0; i < length; i++) {
                Object element = Array.get(o, i);
                IJsonToken value = serialize(element);
                token.add(value);
            }
            return token;
        }

        if (o instanceof Map) {
            IJsonTable token = tokenFactory.newJsonTable();
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) o).entrySet()) {
                String key = entry.getKey().toString();
                IJsonToken value = serialize(entry.getValue());
                token.put(key, value);
            }
            return token;
        } else {
            IJsonTable token = tokenFactory.newJsonTable();
            for (Field field : o.getClass().getFields()) {
                String key = field.getName();
                IJsonToken value;
                try {
                    value = serialize(field, o);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    throw new ParsingException(e);
                }
                token.put(key, value);
            }
            return token;
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

        Iterator<IJsonToken> it = jsonArray.iterator();
        while (it.hasNext()) {
            IJsonToken token = it.next();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            serializeJsonToken(token, margin, indent, eol, numIndents, sb);

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

    private void serializeJsonTable(IJsonTable jsonTable, String margin, String indent, String eol, int numIndents,
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

        if (!jsonTable.isEmpty()) {
            sb.append(eol);
        }

        numIndents++;

        Iterator<Entry<String, IJsonToken>> it = jsonTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, IJsonToken> entry = it.next();
            String key = entry.getKey();
            IJsonToken value = entry.getValue();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            sb.append(Util.serializeToQuotedString(key));
            sb.append(':');
            serializeJsonToken(value, margin, indent, eol, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(eol);
        }

        numIndents--;

        if (!jsonTable.isEmpty()) {
            serializeMarginAndIndents(margin, indent, numIndents, sb);
        }

        sb.append('}');
    }

    private void serializeJsonToken(IJsonToken token, String margin, String indent, String eol, int numIndents,
            StringBuilder sb) {
        if (token instanceof IJsonNull) {
            sb.append("null");
        } else if (token instanceof IJsonBoolean) {
            sb.append(Boolean.toString(token.cast(IJsonBoolean.class).value()));
        } else if (token instanceof IJsonInt) {
            sb.append(Long.toString(token.cast(IJsonInt.class).int64()));
        } else if (token instanceof IJsonFloat) {
            sb.append(Double.toString(token.cast(IJsonFloat.class).float64()));
        } else if (token instanceof IJsonString) {
            sb.append(Util.serializeToQuotedString(token.cast(IJsonString.class).value()));
        } else if (token instanceof IJsonArray) {
            serializeJsonArray(token.cast(IJsonArray.class), margin, indent, eol, numIndents, sb);
        } else if (token instanceof IJsonTable) {
            serializeJsonTable(token.cast(IJsonTable.class), margin, indent, eol, numIndents, sb);
        }
    }

    private void serializeMarginAndIndents(String margin, String indent, int numIndents, StringBuilder sb) {
        sb.append(margin);
        for (int i = 0; i < numIndents; i++) {
            sb.append(indent);
        }
    }
}
