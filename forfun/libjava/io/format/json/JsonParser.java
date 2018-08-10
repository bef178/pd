package libjava.io.format.json;

import static libjava.io.format.ScalarPicker.eatWhitespace;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import libjava.io.ParsingException;
import libjava.io.Pullable;
import libjava.primitive.Ctype;

class JsonParser {

    private static final int STATE_SEGMENT_BEGIN = 0x00;

    private static final int STATE_SEGMENT_END = 0x01;

    private static Json parse(int ch, Pullable it, JsonProducer producer) {
        if (Ctype.isWhitespace(ch)) {
            ch = eatWhitespace(it);
        }
        switch (ch) {
            case '\"':
                return parseScalar(ch, it, producer);
            case '[':
                return parseVector(ch, it, producer);
            case '{':
                return parseObject(ch, it, producer);
            default:
                break;
        }
        throw new ParsingException();
    }

    public static <T extends Json> T parse(Pullable it, JsonProducer producer, Class<T> type) {
        int ch = eatWhitespace(it);
        Json json = parse(ch, it, producer);
        if (type.isInstance(json)) {
            return type.cast(json);
        }
        throw new ParsingException();
    }

    private static Entry<String, Json> parseMapEntry(int ch, Pullable it, JsonProducer producer) {
        String key = parseScalar(ch, it, producer).getString();

        ch = eatWhitespace(it);
        if (ch != ':') {
            throw new ParsingException(':', ch);
        }

        ch = eatWhitespace(it);
        Json value = parse(ch, it, producer);

        return new SimpleImmutableEntry<String, Json>(key, value);
    }

    private static JsonObject parseObject(int ch, Pullable it, JsonProducer producer) {
        if (ch != '{') {
            throw new ParsingException('{', ch);
        }

        JsonObject o = producer.createJsonObject();
        int state = STATE_SEGMENT_BEGIN;

        while (true) {
            ch = eatWhitespace(it);
            switch (state) {
                case STATE_SEGMENT_BEGIN:
                    // accept '}' or string ':' json
                    if (ch == '}') {
                        return o;
                    } else {
                        Entry<String, Json> e = parseMapEntry(ch, it, producer);
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
                        Entry<String, Json> e = parseMapEntry(ch, it, producer);
                        o.put(e.getKey(), e.getValue());
                    } else {
                        throw new ParsingException("expected '}' or ',', actual " + (char) ch);
                    }
                    break;
            }
        }
    }

    private static JsonScalar parseScalar(int ch, Pullable it, JsonProducer producer) {
        if (ch != '\"') {
            throw new ParsingException('\"', ch);
        }
        return producer.createJsonScalar().set(parseString('\"', it).toString());
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

    private static JsonVector parseVector(int ch, Pullable it, JsonProducer producer) {
        if (ch != '[') {
            throw new ParsingException('[', ch);
        }

        JsonVector l = producer.createJsonVector();
        int state = STATE_SEGMENT_BEGIN;

        while (true) {
            ch = eatWhitespace(it);
            switch (state) {
                case STATE_SEGMENT_BEGIN:
                    // accept ']' or json
                    if (ch == ']') {
                        return l;
                    } else {
                        l.insert(parse(ch, it, producer));
                        state = STATE_SEGMENT_END;
                    }
                    break;
                case STATE_SEGMENT_END:
                    // accept ']' or ',' json
                    if (ch == ']') {
                        return l;
                    } else if (ch == ',') {
                        ch = eatWhitespace(it);
                        l.insert(parse(ch, it, producer));
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
