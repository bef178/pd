package pd.codec.json.deserializer;

import pd.codec.HexCodec;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.datatype.JsonArray;
import pd.codec.json.datatype.JsonNull;
import pd.codec.json.datatype.JsonNumber;
import pd.codec.json.datatype.JsonObject;
import pd.codec.json.datatype.JsonString;
import pd.fenc.CharReader;
import pd.fenc.Int32Provider;
import pd.fenc.IWriter;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;
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
        CharReader src = new CharReader(jsonText);
        return deserializeToJson(src);
    }

    private Json deserializeToJson(CharReader src) {
        int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
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

    private JsonArray deserializeToJsonArray(CharReader src) {
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
                    src.eatWhitespaces();
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
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
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

    private Json deserializeToJsonFalse(CharReader src) {
        src.eatOrThrow('f');
        src.eatOrThrow('a');
        src.eatOrThrow('l');
        src.eatOrThrow('s');
        src.eatOrThrow('e');
        return jsonFactory.createJsonBoolean(false);

    }

    private JsonNull deserializeToJsonNull(CharReader src) {
        src.eatOrThrow('n');
        src.eatOrThrow('u');
        src.eatOrThrow('l');
        src.eatOrThrow('l');
        return jsonFactory.getJsonNull();
    }

    private JsonNumber deserializeToJsonNumber(CharReader src) {
        StringBuilder sb = new StringBuilder();
        IWriter dst = IWriter.unicodeStream(sb);
        new ScalarPicker().pickFloat(src, dst);
        return jsonFactory.createJsonNumber().set(sb.toString());
    }

    private JsonObject deserializeToJsonObject(CharReader src) {
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
                    src.eatWhitespaces();
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
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
                    Json pValue = deserializeToJson(src);

                    jsonObject.put(pKey, pValue);
                    src.eatWhitespaces();
                    state = 3;
                    break;
                }
                case 3: {
                    int ch = src.hasNext() ? src.next() : Int32Provider.EOF;
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

    private JsonString deserializeToJsonString(CharReader src) {
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

    private Json deserializeToJsonTrue(CharReader src) {
        src.eatOrThrow('t');
        src.eatOrThrow('r');
        src.eatOrThrow('u');
        src.eatOrThrow('e');
        return jsonFactory.createJsonBoolean(true);
    }
}
