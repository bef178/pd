package pd.codec.json;

import static pd.fenc.IReader.EOF;

import java.lang.reflect.Array;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import pd.codec.HexCodec;
import pd.fenc.CharReader;
import pd.fenc.IWriter;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;
import pd.fenc.Util;
import pd.util.Cascii;

class JsonDeserializer {

    private final IJsonFactory factory;

    public JsonDeserializer(IJsonFactory factory) {
        this.factory = factory;
    }

    /**
     * `IJson` => `Object`
     */
    @SuppressWarnings("unchecked")
    public <T> T convert(IJson json, Class<T> expectedClass) {
        if (IJson.class.isAssignableFrom(expectedClass)) {
            return expectedClass.cast(json);
        }

        if (json instanceof IJsonNull) {
            return null;
        }

        if (expectedClass == boolean.class || expectedClass == Boolean.class) {
            return (T) (Boolean) IJsonBoolean.class.cast(json).getBoolean();
        }

        if (expectedClass == byte.class || expectedClass == Byte.class
                || expectedClass == char.class || expectedClass == Character.class
                || expectedClass == short.class || expectedClass == Short.class
                || expectedClass == int.class || expectedClass == Integer.class
                || expectedClass == long.class || expectedClass == Long.class) {
            return (T) (Long) IJsonNumber.class.cast(json).getInt64();
        }

        if (expectedClass == float.class || expectedClass == Float.class
                || expectedClass == double.class || expectedClass == Double.class) {
            return (T) (Double) IJsonNumber.class.cast(json).getFloat64();
        }

        if (expectedClass == String.class) {
            return (T) IJsonString.class.cast(json).getString();
        }

        if (List.class.isAssignableFrom(expectedClass)) {
            // TODO introduce TypeRegister
            throw new UnsupportedOperationException();
        } else if (expectedClass.isArray()) {
            IJsonArray jsonArray = IJsonArray.class.cast(json);
            Class<?> elementClass = expectedClass.getComponentType();
            Object array = Array.newInstance(elementClass, jsonArray.size());
            for (int i = 0; i < jsonArray.size(); i++) {
                Array.set(array, i, convert(jsonArray.get(i), elementClass));
            }
            return (T) array;
        }

        if (Map.class.isAssignableFrom(expectedClass)) {
            if (expectedClass.isAssignableFrom(LinkedHashMap.class)) {
            }
            // TODO
            throw new UnsupportedOperationException();
        }

        // TODO Object
        throw new UnsupportedOperationException();
    }

    /**
     * `String` => `IJson`
     */
    public IJson deserialize(String jsonText) {
        if (jsonText == null) {
            return null;
        }
        CharReader src = new CharReader(jsonText);
        return deserializeToJson(src);
    }

    private IJson deserializeToJson(CharReader src) {
        int ch = src.hasNext() ? src.next() : EOF;
        switch (ch) {
            case 'n':
                src.moveBack();
                return deserializeToJsonNull(src);
            case 't':
                src.moveBack();
                return deserializeToJsonTrue(src);
            case 'f':
                src.moveBack();
                return deserializeToJsonFalse(src);
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
            case '-':
                src.moveBack();
                return deserializeToJsonNumber(src);
            case '\"':
                src.moveBack();
                return deserializeToJsonString(src);
            case '[':
                src.moveBack();
                return deserializeToJsonArray(src);
            case '{':
                src.moveBack();
                return deserializeToJsonObject(src);
            default:
                throw new ParsingException();
        }
    }

    private IJsonArray deserializeToJsonArray(CharReader src) {
        IJsonArray jsonArray = factory.createJsonArray();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch != '[') {
                        throw new ParsingException(
                                String.format("expected '[', actual [%s]", Util.codepointToString(ch)));
                    }
                    src.eatWhitespaces();
                    state = 1;
                    break;
                }
                case 1: {
                    // a json or the end
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case ']':
                            return jsonArray;
                        default:
                            src.moveBack();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // a json
                    jsonArray.append(deserializeToJson(src));
                    src.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    // a comma or the end
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case ']':
                            return jsonArray;
                        case ',':
                            src.eatWhitespaces();
                            state = 2;
                            break;
                        default:
                            throw new ParsingException(
                                    String.format("expected ']' or ',', actual [%s]", Util.codepointToString(ch)));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJson deserializeToJsonFalse(CharReader src) {
        src.eatOrThrow('f');
        src.eatOrThrow('a');
        src.eatOrThrow('l');
        src.eatOrThrow('s');
        src.eatOrThrow('e');
        return factory.createJsonBoolean(false);

    }

    private IJsonNull deserializeToJsonNull(CharReader src) {
        src.eatOrThrow('n');
        src.eatOrThrow('u');
        src.eatOrThrow('l');
        src.eatOrThrow('l');
        return factory.getJsonNull();
    }

    private IJsonNumber deserializeToJsonNumber(CharReader src) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
        new ScalarPicker().pickFloat(src, dst);
        return factory.createJsonNumber().set(sb.toString());
    }

    private IJsonObject deserializeToJsonObject(CharReader src) {
        IJsonObject jsonObject = factory.createJsonObject();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch != '{') {
                        throw new ParsingException(
                                String.format("expected '{', actual [%s]", Util.codepointToString(ch)));
                    }
                    src.eatWhitespaces();
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case '}':
                            return jsonObject;
                        default:
                            src.moveBack();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // deserializeJsonKeyValue()
                    String pKey = deserializeToJsonString(src).getString();
                    src.eatWhitespaces();
                    src.eatOrThrow(':');
                    src.eatWhitespaces();
                    IJson pValue = deserializeToJson(src);

                    jsonObject.put(pKey, pValue);
                    src.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case '}':
                            return jsonObject;
                        case ',':
                            src.eatWhitespaces();
                            state = 2;
                            break;
                        default:
                            throw new ParsingException(
                                    String.format("expected '}' or ',', actual [%s]", Util.codepointToString(ch)));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJsonString deserializeToJsonString(CharReader src) {
        // state machine go!
        int state = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch != '\"') {
                        throw new ParsingException(
                                String.format("expected '\"', actual [%s]", Util.codepointToString(ch)));
                    }
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = src.hasNext() ? src.next() : EOF;
                    if (ch == '\"') {
                        // consume the end delimiter and exit
                        return factory.createJsonString(sb.toString());
                    } else if (ch == '\\') {
                        state = 2;
                    } else if (Cascii.isControl(ch)) {
                        throw new ParsingException(String.format("unexpected unicode(%d)", ch));
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
                case 2: {
                    // escaping
                    int ch = src.hasNext() ? src.next() : EOF;
                    switch (ch) {
                        case '\"':
                            state = 1;
                            sb.append('\"');
                            break;
                        case '\\':
                            state = 1;
                            sb.append('\\');
                            break;
                        case '/':
                            state = 1;
                            sb.append('/');
                            break;
                        case 'b':
                            state = 1;
                            sb.append('\b');
                            break;
                        case 'f':
                            state = 1;
                            sb.append('\f');
                            break;
                        case 'n':
                            state = 1;
                            sb.append('\n');
                            break;
                        case 'r':
                            state = 1;
                            sb.append('\r');
                            break;
                        case 't':
                            state = 1;
                            sb.append('\t');
                            break;
                        case 'u':
                            int[] u = new int[4];
                            u[0] = src.next();
                            u[1] = src.next();
                            u[2] = src.next();
                            u[3] = src.next();
                            sb.append((char) ((HexCodec.decode1byte(u[0], u[1]) << 8)
                                    | HexCodec.decode1byte(u[2], u[3])));
                            state = 1;
                            break;
                        default:
                            throw new ParsingException(String.format("unexpected [\\%c]", ch));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJson deserializeToJsonTrue(CharReader src) {
        src.eatOrThrow('t');
        src.eatOrThrow('r');
        src.eatOrThrow('u');
        src.eatOrThrow('e');
        return factory.createJsonBoolean(true);
    }
}
