package pd.fenc.json;

import static pd.ctype.Ctype.COMMA;
import static pd.ctype.Ctype.DOUBLE_QUOTE;
import static pd.fenc.IReader.EOF;

import pd.fenc.IReader;
import pd.fenc.IWriter;
import pd.fenc.Int32Scanner;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;

class JsonDeserializer {

    public static final IJsonCreator defaultCreator = new ImplDirectJsonCreator();

    public final IJsonCreator creator;

    // used DirectJson as implementation
    // it should be the only couple point with json implementation
    public JsonDeserializer() {
        this(defaultCreator);
    }

    public JsonDeserializer(IJsonCreator creator) {
        this.creator = creator;
    }

    public IJsonValue deserialize(String jsonCode) {
        Int32Scanner it = new Int32Scanner(IReader.wrap(jsonCode));
        return deserializeToJsonValue(it);
    }

    public <T extends IJsonValue> T deserialize(String jsonCode, Class<T> expectedClass) {
        IJsonValue value = deserialize(jsonCode);
        return creator.cast(value, expectedClass);
    }

    private IJsonArray deserializeToJsonArray(Int32Scanner it) {
        IJsonArray value = creator.newJsonArray();
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
                    // a json value or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return value;
                        default:
                            it.moveBack();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // a json value
                    value.insert(deserializeToJsonValue(it));
                    it.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    // a comma or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return value;
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

    private IJsonValue deserializeToJsonBooleanFalse(Int32Scanner it) {
        it.eatOrThrow('f');
        it.eatOrThrow('a');
        it.eatOrThrow('l');
        it.eatOrThrow('s');
        it.eatOrThrow('e');
        return creator.newJsonBoolean(false);
    }

    private IJsonValue deserializeToJsonBooleanTrue(Int32Scanner it) {
        it.eatOrThrow('t');
        it.eatOrThrow('r');
        it.eatOrThrow('u');
        it.eatOrThrow('e');
        return creator.newJsonBoolean(true);
    }

    private IJsonNull deserializeToJsonNull(Int32Scanner it) {
        it.eatOrThrow('n');
        it.eatOrThrow('u');
        it.eatOrThrow('l');
        it.eatOrThrow('l');
        return creator.newJsonNull();
    }

    private IJsonValue deserializeToJsonNumber(Int32Scanner it) {
        IJsonNumber value = creator.newJsonNumber(0);
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.wrap(sb);
        ScalarPicker.pickFloat(it, dst);
        String raw = sb.toString();
        if (raw.indexOf('.') >= 0) {
            value.set(Double.parseDouble(raw));
        } else {
            value.set(Long.parseLong(raw));
        }
        return value;
    }

    private IJsonObject deserializeToJsonObject(Int32Scanner it) {
        IJsonObject value = creator.newJsonObject();
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
                            return value;
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
                    IJsonValue pValue = deserializeToJsonValue(it);

                    value.put(pKey, pValue);
                    it.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '}':
                            return value;
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

    private IJsonString deserializeToJsonString(Int32Scanner it) {
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
                            return creator.newJsonString(sb.toString());
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

    private IJsonValue deserializeToJsonValue(Int32Scanner it) {
        int ch = it.hasNext() ? it.next() : EOF;
        switch (ch) {
            case 'n':
                it.moveBack();
                return deserializeToJsonNull(it);
            case 't':
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
        throw new ParsingException();
    }
}
