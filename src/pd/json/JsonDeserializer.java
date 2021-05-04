package pd.json;

import static pd.ctype.Ctype.COMMA;
import static pd.ctype.Ctype.DOUBLE_QUOTE;
import static pd.fenc.IReader.EOF;

import pd.fenc.IReader;
import pd.fenc.IWriter;
import pd.fenc.Int32Scanner;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;

public class JsonDeserializer {

    private static class DirectJsonTokenCreator implements IJsonTokenCreator {

        @Override
        public IJsonArray newJsonArray() {
            return new DirectJsonArray();
        }

        @Override
        public DirectJsonBoolean newJsonBoolean(boolean value) {
            return new DirectJsonBoolean(value);
        }

        @Override
        public IJsonFloat newJsonFloat(double value) {
            return new DirectJsonFloat(value);
        }

        @Override
        public IJsonInt newJsonInt(long value) {
            return new DirectJsonInt(value);
        }

        @Override
        public DirectJsonNull newJsonNull() {
            return DirectJsonNull.defaultInstance;
        }

        @Override
        public IJsonString newJsonString(String value) {
            return new DirectJsonString(value);
        }

        @Override
        public IJsonTable newJsonTable() {
            return new DirectJsonTable();
        }
    }

    public interface IJsonTokenCreator {

        public IJsonArray newJsonArray();

        public IJsonBoolean newJsonBoolean(boolean value);

        public IJsonFloat newJsonFloat(double value);

        public IJsonInt newJsonInt(long value);

        public IJsonNull newJsonNull();

        public IJsonString newJsonString(String value);

        public IJsonTable newJsonTable();
    }

    public static final IJsonTokenCreator creator = new DirectJsonTokenCreator();

    public IJsonToken deserialize(String serialized) {
        Int32Scanner it = new Int32Scanner(IReader.wrap(serialized));
        return deserializeToJsonToken(it);
    }

    public <T extends IJsonToken> T deserialize(String serialized, Class<T> expectedClass) {
        return deserialize(serialized).cast(expectedClass);
    }

    private IJsonArray deserializeToJsonArray(Int32Scanner it) {
        IJsonArray token = creator.newJsonArray();
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

    private IJsonToken deserializeToJsonBooleanFalse(Int32Scanner it) {
        it.eatOrThrow('f');
        it.eatOrThrow('a');
        it.eatOrThrow('l');
        it.eatOrThrow('s');
        it.eatOrThrow('e');
        return creator.newJsonBoolean(false);
    }

    private IJsonToken deserializeToJsonBooleanTrue(Int32Scanner it) {
        it.eatOrThrow('t');
        it.eatOrThrow('r');
        it.eatOrThrow('u');
        it.eatOrThrow('e');
        return creator.newJsonBoolean(true);
    }

    private IJsonToken deserializeToJsonIntOrJsonFloat(Int32Scanner it) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.wrap(sb);
        ScalarPicker.pickFloat(it, dst);
        String raw = sb.toString();
        if (raw.indexOf('.') >= 0) {
            return creator.newJsonFloat(Double.parseDouble(raw));
        } else {
            return creator.newJsonFloat(Long.parseLong(raw));
        }
    }

    private IJsonNull deserializeToJsonNull(Int32Scanner it) {
        it.eatOrThrow('n');
        it.eatOrThrow('u');
        it.eatOrThrow('l');
        it.eatOrThrow('l');
        return creator.newJsonNull();
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

    private IJsonTable deserializeToJsonTable(Int32Scanner it) {
        IJsonTable token = creator.newJsonTable();
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

    private IJsonToken deserializeToJsonToken(Int32Scanner it) {
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
                return deserializeToJsonIntOrJsonFloat(it);
            case '\"':
                it.moveBack();
                return deserializeToJsonString(it);
            case '[':
                it.moveBack();
                return deserializeToJsonArray(it);
            case '{':
                it.moveBack();
                return deserializeToJsonTable(it);
            default:
                break;
        }
        throw new ParsingException();
    }
}
