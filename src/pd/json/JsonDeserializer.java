package pd.json;

import static pd.fenc.IReader.EOF;

import java.lang.reflect.Array;

import pd.fenc.Cascii;
import pd.fenc.CharReader;
import pd.fenc.HexCodec;
import pd.fenc.IWriter;
import pd.fenc.ScalarPicker;
import pd.fenc.Util;
import pd.json.type.IJson;
import pd.json.type.IJsonArray;
import pd.json.type.IJsonBoolean;
import pd.json.type.IJsonNull;
import pd.json.type.IJsonNumber;
import pd.json.type.IJsonObject;
import pd.json.type.IJsonString;

class JsonDeserializer {

    private final IJsonFactory factory;

    public JsonDeserializer(IJsonFactory factory) {
        this.factory = factory;
    }

    /**
     * `IJson` => `Object`
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(IJson json, Class<T> expectedJavaClass) {
        if (json instanceof IJsonNull) {
            return null;
        }

        if (expectedJavaClass == boolean.class || expectedJavaClass == Boolean.class) {
            return (T) (Boolean) IJsonBoolean.class.cast(json).getBoolean();
        }

        if (expectedJavaClass == byte.class || expectedJavaClass == Byte.class
                || expectedJavaClass == char.class || expectedJavaClass == Character.class
                || expectedJavaClass == short.class || expectedJavaClass == Short.class
                || expectedJavaClass == int.class || expectedJavaClass == Integer.class
                || expectedJavaClass == long.class || expectedJavaClass == Long.class) {
            return (T) (Long) IJsonNumber.class.cast(json).getInt64();
        }

        if (expectedJavaClass == float.class || expectedJavaClass == Float.class
                || expectedJavaClass == double.class || expectedJavaClass == Double.class) {
            return (T) (Double) IJsonNumber.class.cast(json).getFloat64();
        }

        if (expectedJavaClass == String.class) {
            return (T) IJsonString.class.cast(json).getString();
        }

        if (expectedJavaClass.isArray()) {
            IJsonArray jsonArray = IJsonArray.class.cast(json);
            Class<?> elementClass = expectedJavaClass.getComponentType();
            Object array = Array.newInstance(elementClass, jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                Array.set(array, i, convert(jsonArray.get(i), elementClass));
            }
            return (T) array;
        }

        // TODO Object

        throw new UnsupportedOperationException();
    }

    /**
     * `String` => `IJson`
     */
    public IJson deserialize(String jsonCode) {
        CharReader it = new CharReader(jsonCode);
        return deserializeToJson(it);
    }

    private IJsonArray deserializeToJsonArray(CharReader it) {
        IJsonArray jsonArray = factory.newJsonArray();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '[') {
                        throw new JsonException(String.format("expected '[', actual [%s]", Util.codepointToString(ch)));
                    }
                    it.eatWhitespaces();
                    state = 1;
                    break;
                }
                case 1: {
                    // a json or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return jsonArray;
                        default:
                            it.moveBack();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // a json
                    jsonArray.append(deserializeToJson(it));
                    it.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    // a comma or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return jsonArray;
                        case ',':
                            it.eatWhitespaces();
                            state = 2;
                            break;
                        default:
                            throw new JsonException(
                                    String.format("expected ']' or ',', actual [%s]", Util.codepointToString(ch)));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJson deserializeToJsonFalse(CharReader it) {
        it.eatOrThrow('f');
        it.eatOrThrow('a');
        it.eatOrThrow('l');
        it.eatOrThrow('s');
        it.eatOrThrow('e');
        return factory.newJsonBoolean(false);
    }

    private IJson deserializeToJsonTrue(CharReader it) {
        it.eatOrThrow('t');
        it.eatOrThrow('r');
        it.eatOrThrow('u');
        it.eatOrThrow('e');
        return factory.newJsonBoolean(true);
    }

    private IJson deserializeToJson(CharReader it) {
        int ch = it.hasNext() ? it.next() : EOF;
        switch (ch) {
            case 'n':
                it.moveBack();
                return deserializeToJsonNull(it);
            case 't':
                it.moveBack();
                return deserializeToJsonTrue(it);
            case 'f':
                it.moveBack();
                return deserializeToJsonFalse(it);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                it.moveBack();
                return deserializeToJsonNumber(it);
            case '\"':
                it.moveBack();
                return deserializeToJsonString(it);
            case '[':
                it.moveBack();
                return deserializeToJsonArray(it);
            case '{':
                it.moveBack();
                return deserializeToJsonObject(it);
            default:
                break;
        }
        throw new JsonException();
    }

    private IJsonNumber deserializeToJsonNumber(CharReader it) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
        ScalarPicker.pickFloat(it, dst);
        String raw = sb.toString();
        return factory.newJsonNumber().set(raw);
    }

    private IJsonNull deserializeToJsonNull(CharReader it) {
        it.eatOrThrow('n');
        it.eatOrThrow('u');
        it.eatOrThrow('l');
        it.eatOrThrow('l');
        return factory.newJsonNull();
    }

    private IJsonObject deserializeToJsonObject(CharReader it) {
        IJsonObject jsonObject = factory.newJsonObject();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '{') {
                        throw new JsonException(String.format("expected '{', actual [%s]", Util.codepointToString(ch)));
                    }
                    it.eatWhitespaces();
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '}':
                            return jsonObject;
                        default:
                            it.moveBack();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // deserializeJsonKeyValue()
                    String pKey = deserializeToJsonString(it).getString();
                    it.eatWhitespaces();
                    it.eatOrThrow(':');
                    it.eatWhitespaces();
                    IJson pValue = deserializeToJson(it);

                    jsonObject.put(pKey, pValue);
                    it.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '}':
                            return jsonObject;
                        case ',':
                            it.eatWhitespaces();
                            state = 2;
                            break;
                        default:
                            throw new JsonException(
                                    String.format("expected '}' or ',', actual [%s]", Util.codepointToString(ch)));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJsonString deserializeToJsonString(CharReader it) {
        // state machine go!
        int state = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '\"') {
                        throw new JsonException(
                                String.format("expected '\"', actual [%s]", Util.codepointToString(ch)));
                    }
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch == '\"') {
                        // consume the end delimiter and exit
                        return factory.newJsonString().set(sb.toString());
                    } else if (ch == '\\') {
                        state = 2;
                    } else if (Cascii.isControl(ch)) {
                        throw new JsonException(String.format("unexpected unicode(%d)", ch));
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
                case 2: {
                    // escaping
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '\"':
                            sb.append('\"');
                            state = 1;
                            break;
                        case '\\':
                            sb.append('\\');
                            state = 1;
                            break;
                        case '/':
                            sb.append('/');
                            state = 1;
                            break;
                        case 'b':
                            sb.append('\b');
                            state = 1;
                            break;
                        case 'f':
                            sb.append('\f');
                            state = 1;
                            break;
                        case 'n':
                            sb.append('\n');
                            state = 1;
                            break;
                        case 'r':
                            sb.append('\r');
                            state = 1;
                            break;
                        case 't':
                            sb.append('\t');
                            state = 1;
                            break;
                        case 'u':
                            int[] u = new int[4];
                            u[0] = it.next();
                            u[1] = it.next();
                            u[2] = it.next();
                            u[3] = it.next();
                            sb.append((char) ((HexCodec.decode1byte(u[0], u[1]) << 8)
                                    | HexCodec.decode1byte(u[2], u[3])));
                            state = 1;
                            break;
                        default:
                            throw new JsonException(String.format("unexpected [\\%c]", ch));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }
}
