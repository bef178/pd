package pd.codec.json.deserializer;

import pd.codec.HexCodec;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.datatype.JsonArray;
import pd.codec.json.datatype.JsonNull;
import pd.codec.json.datatype.JsonNumber;
import pd.codec.json.datatype.JsonObject;
import pd.codec.json.datatype.JsonString;
import pd.fenc.IWriter;
import pd.fenc.Int32Provider;
import pd.fenc.NumberPicker;
import pd.fenc.ParsingException;
import pd.fenc.UnicodeProvider;
import pd.fenc.Util;
import pd.util.AsciiExtension;

class DeserializeToJsonExecutor {

    private final JsonFactory jsonFactory;

    public DeserializeToJsonExecutor(JsonFactory jsonFactory) {
        this.jsonFactory = jsonFactory;
    }

    /**
     * `String` => `Json`<br/>
     */
    public Json deserialize(String jsonText) {
        if (jsonText == null) {
            return null;
        }
        UnicodeProvider src = new UnicodeProvider(jsonText);
        return deserializeToJson(src);
    }

    private Json deserializeToJson(UnicodeProvider src) {
        int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
        switch (ch) {
            case 'n':
                src.back();
                return deserializeToJsonNull(src);
            case 't':
                src.back();
                return deserializeToJsonTrue(src);
            case 'f':
                src.back();
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
                src.back();
                return deserializeToJsonNumber(src);
            case '\"':
                src.back();
                return deserializeToJsonString(src);
            case '[':
                src.back();
                return deserializeToJsonArray(src);
            case '{':
                src.back();
                return deserializeToJsonObject(src);
            default:
                throw new ParsingException();
        }
    }

    private JsonArray deserializeToJsonArray(UnicodeProvider src) {
        JsonArray jsonArray = jsonFactory.createJsonArray();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    if (ch != '[') {
                        throw new ParsingException(
                                String.format("expected '[', actual [%s]", Util.codepointToString(ch)));
                    }
                    src.eatWhitespacesIfAny();
                    state = 1;
                    break;
                }
                case 1: {
                    // a json or the end
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    switch (ch) {
                        case ']':
                            return jsonArray;
                        default:
                            src.back();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // a json
                    jsonArray.append(deserializeToJson(src));
                    src.eatWhitespacesIfAny();
                    state = 3;
                    break;
                }
                case 3: {
                    // a comma or the end
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    switch (ch) {
                        case ']':
                            return jsonArray;
                        case ',':
                            src.eatWhitespacesIfAny();
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

    private Json deserializeToJsonFalse(UnicodeProvider src) {
        src.eat('f');
        src.eat('a');
        src.eat('l');
        src.eat('s');
        src.eat('e');
        return jsonFactory.createJsonBoolean(false);

    }

    private JsonNull deserializeToJsonNull(UnicodeProvider src) {
        src.eat('n');
        src.eat('u');
        src.eat('l');
        src.eat('l');
        return jsonFactory.getJsonNull();
    }

    private JsonNumber deserializeToJsonNumber(UnicodeProvider src) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
        new NumberPicker().pickFloat(src, dst);
        return jsonFactory.createJsonNumber().set(sb.toString());
    }

    private JsonObject deserializeToJsonObject(UnicodeProvider src) {
        JsonObject jsonObject = jsonFactory.createJsonObject();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    if (ch != '{') {
                        throw new ParsingException(
                                String.format("expected '{', actual [%s]", Util.codepointToString(ch)));
                    }
                    src.eatWhitespacesIfAny();
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    switch (ch) {
                        case '}':
                            return jsonObject;
                        default:
                            src.back();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // deserializeJsonKeyValue()
                    String pKey = deserializeToJsonString(src).getString();
                    src.eatWhitespacesIfAny();
                    src.eat(':');
                    src.eatWhitespacesIfAny();
                    Json pValue = deserializeToJson(src);

                    jsonObject.put(pKey, pValue);
                    src.eatWhitespacesIfAny();
                    state = 3;
                    break;
                }
                case 3: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    switch (ch) {
                        case '}':
                            return jsonObject;
                        case ',':
                            src.eatWhitespacesIfAny();
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

    private JsonString deserializeToJsonString(UnicodeProvider src) {
        // state machine go!
        int state = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            switch (state) {
                case 0: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    if (ch != '\"') {
                        throw new ParsingException(
                                String.format("expected '\"', actual [%s]", Util.codepointToString(ch)));
                    }
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
                    if (ch == '\"') {
                        // consume the end delimiter and exit
                        return jsonFactory.createJsonString(sb.toString());
                    } else if (ch == '\\') {
                        state = 2;
                    } else if (AsciiExtension.isControl(ch)) {
                        throw new ParsingException(String.format("unexpected unicode(%d)", ch));
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
                case 2: {
                    // escaping
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
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

    private Json deserializeToJsonTrue(UnicodeProvider src) {
        src.eat('t');
        src.eat('r');
        src.eat('u');
        src.eat('e');
        return jsonFactory.createJsonBoolean(true);
    }
}
