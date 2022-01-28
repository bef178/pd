package pd.json;

import static pd.fenc.Cascii.COMMA;
import static pd.fenc.Cascii.DOUBLE_QUOTE;
import static pd.fenc.IReader.EOF;
import static pd.json.JsonCodec.tokenFactory;

import java.lang.reflect.Array;

import pd.fenc.CharReader;
import pd.fenc.IWriter;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;

class JsonDeserializer {

    /**
     * `IJsonToken` => java object
     */
    @SuppressWarnings("unchecked")
    public <T> T deserialize(IJsonToken token, Class<T> expectedClass) {
        if (token instanceof IJsonNull) {
            return null;
        } else if (expectedClass == boolean.class || expectedClass == Boolean.class) {
            return (T) (Boolean) token.cast(IJsonBoolean.class).value();
        } else if (expectedClass == byte.class || expectedClass == Byte.class
                || expectedClass == char.class || expectedClass == Character.class
                || expectedClass == short.class || expectedClass == Short.class
                || expectedClass == int.class || expectedClass == Integer.class
                || expectedClass == long.class || expectedClass == Long.class) {
            return (T) (Long) token.cast(IJsonInt.class).int64();
        } else if (expectedClass == float.class || expectedClass == Float.class
                || expectedClass == double.class || expectedClass == Double.class) {
            return (T) (Double) token.cast(IJsonFloat.class).float64();
        } else if (expectedClass == String.class) {
            return (T) token.cast(IJsonString.class).value();
        }

        if (expectedClass.isArray()) {
            IJsonArray arrayToken = token.cast(IJsonArray.class);
            Class<?> elementClass = expectedClass.getComponentType();
            Object array = Array.newInstance(elementClass, arrayToken.size());
            for (int i = 0; i < arrayToken.size(); i++) {
                Array.set(array, i, deserialize(arrayToken.get(i), elementClass));
            }
            return (T) array;
        }

        throw new UnsupportedOperationException();
    }

    /**
     * `String` => `IJsonToken`<br/>
     */
    public IJsonToken deserialize(String serialized) {
        CharReader it = new CharReader(serialized);
        return deserializeToJsonToken(it);
    }

    private IJsonArray deserializeToJsonArray(CharReader it) {
        IJsonArray token = tokenFactory.newJsonArray();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '[') {
                        throw new ParsingException('[', ch);
                    }
                    it.eatWhitespaces();
                    state = 1;
                    break;
                }
                case 1: {
                    // a json token or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return token;
                        default:
                            it.moveBack();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // a json token
                    token.insert(deserializeToJsonToken(it));
                    it.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    // a comma or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return token;
                        case COMMA:
                            it.eatWhitespaces();
                            state = 2;
                            break;
                        default:
                            String actual = new String(Character.toChars(ch));
                            throw new ParsingException(String
                                    .format("expected ']' or ',' while actual [%s]", actual));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJsonToken deserializeToJsonBooleanFalse(CharReader it) {
        it.eatOrThrow('f');
        it.eatOrThrow('a');
        it.eatOrThrow('l');
        it.eatOrThrow('s');
        it.eatOrThrow('e');
        return tokenFactory.newJsonBoolean(false);
    }

    private IJsonToken deserializeToJsonBooleanTrue(CharReader it) {
        it.eatOrThrow('t');
        it.eatOrThrow('r');
        it.eatOrThrow('u');
        it.eatOrThrow('e');
        return tokenFactory.newJsonBoolean(true);
    }

    private IJsonToken deserializeToJsonIntOrJsonFloat(CharReader it) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
        ScalarPicker.pickFloat(it, dst);
        String raw = sb.toString();
        if (raw.indexOf('.') >= 0) {
            return tokenFactory.newJsonFloat(Double.parseDouble(raw));
        } else {
            return tokenFactory.newJsonInt(Long.parseLong(raw));
        }
    }

    private IJsonNull deserializeToJsonNull(CharReader it) {
        it.eatOrThrow('n');
        it.eatOrThrow('u');
        it.eatOrThrow('l');
        it.eatOrThrow('l');
        return tokenFactory.newJsonNull();
    }

    private IJsonString deserializeToJsonString(CharReader it) {
        // state machine go!
        int state = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != DOUBLE_QUOTE) {
                        throw new ParsingException(DOUBLE_QUOTE, ch);
                    }
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case DOUBLE_QUOTE:
                            // consume the end delimiter and exit
                            return tokenFactory.newJsonString(sb.toString());
                        case '\\':
                            state = 2;
                            break;
                        default:
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
                case 2: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case DOUBLE_QUOTE:
                            sb.append(DOUBLE_QUOTE);
                            state = 1;
                            break;
                        case '\\':
                            sb.append('\\');
                            state = 1;
                            break;
                        default:
                            String actual = new String(Character.toChars(ch));
                            throw new ParsingException(String
                                    .format("expected [\"] or [\\] while actual [%s]", actual));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJsonObject deserializeToJsonObject(CharReader it) {
        IJsonObject token = tokenFactory.newJsonObject();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '{') {
                        throw new ParsingException('{', ch);
                    }
                    it.eatWhitespaces();
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '}':
                            return token;
                        default:
                            it.moveBack();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // deserializeJsonKeyValue()
                    String pKey = deserializeToJsonString(it).value();
                    it.eatWhitespaces();
                    it.eatOrThrow(':');
                    it.eatWhitespaces();
                    IJsonToken pValue = deserializeToJsonToken(it);

                    token.put(pKey, pValue);
                    it.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '}':
                            return token;
                        case COMMA:
                            it.eatWhitespaces();
                            state = 2;
                            break;
                        default:
                            String actual = new String(Character.toChars(ch));
                            throw new ParsingException(String
                                    .format("expected '}' or ',' while actual [%s]", actual));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private IJsonToken deserializeToJsonToken(CharReader it) {
        int ch = it.hasNext() ? it.next() : EOF;
        switch (ch) {
            case 'n':
                it.moveBack();
                return deserializeToJsonNull(it);
            case 't':
                it.moveBack();
                return deserializeToJsonBooleanTrue(it);
            case 'f':
                it.moveBack();
                return deserializeToJsonBooleanFalse(it);
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
                return deserializeToJsonIntOrJsonFloat(it);
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
        throw new ParsingException();
    }
}
