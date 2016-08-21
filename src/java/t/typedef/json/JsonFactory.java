package t.typedef.json;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import t.typedef.Ctype;
import t.typedef.basic.Blob;
import t.typedef.io.Factory;
import t.typedef.io.FormatCodec;
import t.typedef.io.InstallmentByteBuffer;
import t.typedef.io.ParsingException;
import t.typedef.io.InstallmentByteBuffer.Reader;

public class JsonFactory {

    private static class Builder {

        private static final int STATE_SEGMENT_BEGIN = 0x00;
        private static final int STATE_SEGMENT_END = 0x01;

        private static void skipWhitespaces(Reader r) {
            while (r.hasNext()) {
                if (Ctype.isWhitespace(r.peek())) {
                    r.next();
                } else {
                    break;
                }
            }
        }

        private static Json build(Reader r, Json.Producer producer) {
            skipWhitespaces(r);
            switch (r.peek()) {
                case '"':
                    return buildS(r, producer);
                case '[':
                    return buildQ(r, producer);
                case '{':
                    return buildM(r, producer);
                default:
                    throw new ParsingException();
            }
        }

        private static Entry<String, Json> buildEntry(Reader r,
                Json.Producer producer) {
            String key = buildS(r, producer).getString();

            skipWhitespaces(r);
            int ch = r.next();
            if (ch != ':') {
                throw new ParsingException(":", ch);
            }

            skipWhitespaces(r);
            Json value = build(r, producer);

            return new SimpleImmutableEntry<String, Json>(key, value);
        }

        private static JsonMapping buildM(Reader r,
                Json.Producer producer) {
            int ch = r.next();
            if (ch != '{') {
                throw new ParsingException("{", ch);
            }

            JsonMapping M = producer.produceMapping();
            int state = STATE_SEGMENT_BEGIN;

            while (true) {
                skipWhitespaces(r);
                ch = r.next();
                switch (state) {
                    case STATE_SEGMENT_BEGIN:
                        // accept '}' or string ':' json
                        if (ch == '}') {
                            return M;
                        } else {
                            r.putBack();
                            Entry<String, Json> e = buildEntry(r, producer);
                            M.put(e.getKey(), e.getValue());
                            state = STATE_SEGMENT_END;
                        }
                        break;
                    case STATE_SEGMENT_END:
                        // accept '}' or ',' string ':' json
                        if (ch == '}') {
                            return M;
                        } else if (ch == ',') {
                            skipWhitespaces(r);
                            Entry<String, Json> e = buildEntry(r, producer);
                            M.put(e.getKey(), e.getValue());
                        } else {
                            throw new ParsingException(",", ch);
                        }
                        break;
                }
            }
        }

        private static JsonSequence buildQ(Reader r,
                Json.Producer producer) {
            int ch = r.next();
            if (ch != '[') {
                throw new ParsingException("[", ch);
            }

            JsonSequence Q = producer.produceSequence();
            int state = STATE_SEGMENT_BEGIN;

            while (true) {
                skipWhitespaces(r);
                ch = r.next();
                switch (state) {
                    case STATE_SEGMENT_BEGIN:
                        // accept ']' or json
                        if (ch == ']') {
                            return Q;
                        } else {
                            r.putBack();
                            Q.insert(build(r, producer));
                            state = STATE_SEGMENT_END;
                        }
                        break;
                    case STATE_SEGMENT_END:
                        // accept ']' or ',' json
                        if (ch == ']') {
                            return Q;
                        } else if (ch == ',') {
                            skipWhitespaces(r);
                            Q.insert(build(r, producer));
                        } else {
                            throw new ParsingException(",", ch);
                        }
                        break;
                }
            }
        }

        private static JsonScalar buildS(final Reader r,
                Json.Producer producer) {
            return producer.produceScalar()
                    .set(Factory.fromScalar(r).toString());
        }
    }

    private static class Serializer {

        private static InstallmentByteBuffer serialize(Json json,
                String prefix, final String INNER_PREFIX, final String LF,
                InstallmentByteBuffer o) {
            switch (json.type()) {
                case SCALAR:
                    serializeS((JsonScalar) json, o);
                    break;
                case SEQUENCE:
                    serializeQ((JsonSequence) json, prefix,
                            INNER_PREFIX, LF, o);
                    break;
                case MAPPING:
                    serializeM((JsonMapping) json, prefix,
                            INNER_PREFIX, LF, o);
                    break;
                default:
                    throw new Json.IllegalTypeException();
            }
            return o;
        }

        private static void serializeM(JsonMapping jsonM,
                String prefix, final String INNER_PREFIX, final String LF,
                InstallmentByteBuffer o) {
            final String prefix1 = prefix + INNER_PREFIX;
            boolean isEmpty = jsonM.isEmpty();

            o.append('{');
            if (!isEmpty) {
                o.append(LF);
            }

            List<String> keys = new ArrayList<String>(jsonM.keys());
            Collections.sort(keys);
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                o.append(prefix1);
                String key = it.next();
                Json value = jsonM.getJson(key);

                serializeS(key, o);
                o.append(':');
                serialize(value, prefix1, INNER_PREFIX, LF, o);

                if (it.hasNext()) {
                    o.append(",");
                }
                o.append(LF);
            }

            if (!isEmpty) {
                o.append(prefix);
            }
            o.append('}');
        }

        private static void serializeQ(JsonSequence jsonQ,
                String prefix, final String INNER_PREFIX, final String LF,
                InstallmentByteBuffer o) {
            final String prefix1 = prefix + INNER_PREFIX;
            final String lineEnd = LF;
            boolean isEmpty = jsonQ.isEmpty();

            o.append('[');
            if (!isEmpty) {
                o.append(lineEnd);
            }

            for (int i = 0; i < jsonQ.size(); ++i) {
                o.append(prefix1);
                serialize(jsonQ.getJson(i), prefix1, INNER_PREFIX, LF, o);
                if (i < jsonQ.size() - 1) {
                    o.append(",");
                }
                o.append(lineEnd);
            }

            if (!isEmpty) {
                o.append(prefix);
            }
            o.append(']');
        }

        private static void serializeS(JsonScalar jsonS,
                InstallmentByteBuffer o) {
            serializeS(jsonS.getString(), o);
        }

        private static void serializeS(String s, InstallmentByteBuffer o) {
            o.append('\"');
            for (int i = 0; i < s.length(); ++i) {

                char c = s.charAt(i);
                if (Ctype.isAlphanum(c)) {
                    o.append(c);
                    continue;
                }

                int ch = c;
                if (Character.isHighSurrogate(c)) {
                    char chLow = s.charAt(++i);
                    if (Character.isLowSurrogate(chLow)) {
                        ch = Character.toCodePoint(c, chLow);
                    } else {
                        throw new ParsingException();
                    }
                } else if (Character.isLowSurrogate(c)) {
                    throw new ParsingException();
                }
                // faster than String.getBytes("UTF-8") with exception handled
                // faster than toUtf8() then toHexText()
                Blob blob = new Blob();
                FormatCodec.PrivateContract.encode(ch, blob);
                o.append(blob.a);
            }
            o.append('\"');
        }
    }

    public static Json build(String src, Json.Producer producer) {
        Reader r = new InstallmentByteBuffer().append(src).reader();
        return Builder.build(r, producer);
    }

    public static boolean equals(Json json, Object o) {
        if (json == o) {
            return true;
        } else if (o instanceof Json) {
            Json j = (Json) o;
            if (json.type() == j.type()) {
                String s1 = serialize(json);
                String s2 = serialize(j);
                return s1.equals(s2);
            }
        }
        return false;
    }

    public static String serialize(Json json) {
        return serializeAsCheatSheet(json, "");
    }

    public static String serializeAsCheatSheet(Json json, String prefix) {
        return Serializer.serialize(json, prefix,
                "", "",
                new InstallmentByteBuffer()).toString();
    }

    public static String serializeAsWellFormed(Json json, String prefix) {
        return Serializer.serialize(json, prefix,
                "  ", System.getProperty("line.separator"),
                new InstallmentByteBuffer()).toString();
    }

    private JsonFactory() {
        // private dummy
    }
}
