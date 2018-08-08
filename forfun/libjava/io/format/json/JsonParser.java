package libjava.io.format.json;

import static libjava.io.format.ScalarPicker.eatWhitespace;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.primitive.Ctype;

public class JsonParser {

    private static final int STATE_SEGMENT_BEGIN = 0x00;

    private static final int STATE_SEGMENT_END = 0x01;

    private static Json parse(int ch, Pullable it, JsonFactory factory) {
        if (Ctype.isWhitespace(ch)) {
            ch = eatWhitespace(it);
        }
        switch (ch) {
            case '\"':
                return parseScalar(ch, it, factory);
            case '[':
                return parseVector(ch, it, factory);
            case '{':
                return parseObject(ch, it, factory);
            default:
                break;
        }
        throw new ParsingException();
    }

    public static Json parse(Pullable it, JsonFactory factory) {
        return parse(it, factory, Json.class);
    }

    public static <T extends Json> T parse(Pullable it, JsonFactory factory, Class<T> type) {
        int ch = eatWhitespace(it);
        Json json = parse(ch, it, factory);
        if (type.isInstance(json)) {
            return type.cast(json);
        }
        throw new ParsingException();
    }

    private static Entry<String, Json> parseMapEntry(int ch, Pullable it,
            JsonFactory factory) {
        String key = parseScalar(ch, it, factory).getString();

        ch = eatWhitespace(it);
        if (ch != ':') {
            throw new ParsingException(':', ch);
        }

        ch = eatWhitespace(it);
        Json value = parse(ch, it, factory);

        return new SimpleImmutableEntry<String, Json>(key, value);
    }

    private static JsonObject parseObject(int ch, Pullable it, JsonFactory factory) {
        if (ch != '{') {
            throw new ParsingException('{', ch);
        }

        JsonObject o = factory.createJsonObject();
        int state = STATE_SEGMENT_BEGIN;

        while (true) {
            ch = eatWhitespace(it);
            switch (state) {
                case STATE_SEGMENT_BEGIN:
                    // accept '}' or string ':' json
                    if (ch == '}') {
                        return o;
                    } else {
                        Entry<String, Json> e = parseMapEntry(ch, it,
                                factory);
                        o.put(e.getKey(), e.getValue());
                        state = STATE_SEGMENT_END;
                    }
                    break;
                case STATE_SEGMENT_END:
                    // accept '}' or ',' string ':' json
                    if (ch == '}') {
                        return o;
                    } else if (ch == ',') {
                        ch = eatWhitespace(it);
                        Entry<String, Json> e = parseMapEntry(ch, it,
                                factory);
                        o.put(e.getKey(), e.getValue());
                    } else {
                        throw new ParsingException(
                                "expected '}' or ',', actual " + (char) ch);
                    }
                    break;
            }
        }
    }

    private static JsonScalar parseScalar(int ch, Pullable it, JsonFactory factory) {
        if (ch != '\"') {
            throw new ParsingException('\"', ch);
        }
        return factory.createJsonScalar()
                .set(parseString('\"', it).toString());
    }

    private static CharSequence parseString(int closingSymbol, Pullable it) {
        boolean isEscaping = false;
        StringBuilder sb = new StringBuilder();
        int ch = it.pull();
        while (ch != -1) {
            if (isEscaping) {
                isEscaping = false;
                if (ch == '\"') {
                    sb.append('\"');
                } else {
                    sb.append('\\').appendCodePoint(ch);
                }
            } else {
                if (ch == closingSymbol) {
                    // consume the end delimiter then exit
                    return sb;
                } else if (ch == '\\') {
                    isEscaping = true;
                } else {
                    sb.appendCodePoint(ch);
                }
            }
            ch = it.pull();
        }
        throw new ParsingException();
    }

    private static JsonVector parseVector(int ch, Pullable it,
            JsonFactory factory) {
        if (ch != '[') {
            throw new ParsingException('[', ch);
        }

        JsonVector l = factory.createJsonVector();
        int state = STATE_SEGMENT_BEGIN;

        while (true) {
            ch = eatWhitespace(it);
            switch (state) {
                case STATE_SEGMENT_BEGIN:
                    // accept ']' or json
                    if (ch == ']') {
                        return l;
                    } else {
                        l.insert(parse(ch, it, factory));
                        state = STATE_SEGMENT_END;
                    }
                    break;
                case STATE_SEGMENT_END:
                    // accept ']' or ',' json
                    if (ch == ']') {
                        return l;
                    } else if (ch == ',') {
                        ch = eatWhitespace(it);
                        l.insert(parse(ch, it, factory));
                    } else {
                        throw new ParsingException(
                                "expected ']' or ',', actual " + (char) ch);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private JsonParser() {
        // dummy
    }
}
