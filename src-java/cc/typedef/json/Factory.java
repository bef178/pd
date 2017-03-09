package cc.typedef.json;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import cc.typedef.adt.Blob;
import cc.typedef.io.Codec;
import cc.typedef.io.InstallmentByteBuffer;
import cc.typedef.io.InstallmentByteBuffer.Reader;
import cc.typedef.io.Nextable;
import cc.typedef.io.ParsingException;
import cc.typedef.primitive.Ctype;

public class Factory {

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

        private static Json build(Reader r, Producer producer) {
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
                Producer producer) {
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

        private static JsonDict buildM(Reader r, Producer producer) {
            int ch = r.next();
            if (ch != '{') {
                throw new ParsingException("{", ch);
            }

            JsonDict M = producer.produceJsonDict();
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

        private static JsonList buildQ(Reader r, Producer producer) {
            int ch = r.next();
            if (ch != '[') {
                throw new ParsingException("[", ch);
            }

            JsonList Q = producer.produceJsonList();
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

        private static JsonScalar buildS(Nextable it, Producer producer) {
            return producer.produceJsonScalar()
                    .set(parseString(it).toString());
        }

        /**
         * accept a quoted string
         */
        public static CharSequence parseString(Nextable it) {
            return parseString(it, '\"', '\"');
        }

        public static CharSequence parseString(Nextable it, final int start,
                final int end) {
            int ch = -1;
            if (start >= 0) {
                // there is a start delimiter, taste it
                ch = it.next();
                if (ch != start) {
                    throw new ParsingException(start, ch);
                }
            }

            StringBuilder sb = new StringBuilder();
            while (true) {
                ch = it.next();
                if (ch == end) {
                    // consume the end delimiter then exit
                    return sb;
                }

                if (ch == '\\') {
                    ch = Codec.Glyph.decode(ch, it);
                }
                sb.appendCodePoint(ch);
            }
        }
    }

    private static class Serializer {

        public static void serialize(CharSequence s, InstallmentByteBuffer w) {
            w.append('\"');
            for (int i = 0; i < s.length(); ++i) {
                char c = s.charAt(i);
                if (Ctype.isAlphanum(c)) {
                    w.append(c);
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
                Codec.Glyph.encode(ch, blob);
                w.append(blob.a);
            }
            w.append('\"');
        }

        private static InstallmentByteBuffer serialize(Json json,
                Config config, InstallmentByteBuffer o) {
            switch (json.type()) {
                case SCALAR:
                    serializeS((JsonScalar) json, o);
                    break;
                case LIST:
                    serializeQ((JsonList) json, config, o);
                    break;
                case DICT:
                    serializeM((JsonDict) json, config, o);
                    break;
                default:
                    throw new IllegalTypeException();
            }
            return o;
        }

        private static void serializeM(JsonDict json, Config config,
                InstallmentByteBuffer o) {
            boolean isEmpty = json.isEmpty();

            o.append('{');
            if (!isEmpty) {
                o.append(config.eol);
            }

            config.indentCount++;
            List<String> keys = new ArrayList<String>(json.keys());
            Collections.sort(keys);
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                config.printWhitespace(o);
                String key = it.next();
                Json value = json.getJson(key);

                serializeS(key, o);
                o.append(':');
                serialize(value, config, o);

                if (it.hasNext()) {
                    o.append(",");
                }
                o.append(config.eol);
            }
            config.indentCount--;

            if (!isEmpty) {
                config.printWhitespace(o);
            }
            o.append('}');
        }

        private static void serializeQ(JsonList json, Config config,
                InstallmentByteBuffer o) {
            boolean isEmpty = json.isEmpty();

            o.append('[');
            if (!isEmpty) {
                o.append(config.eol);
            }

            config.indentCount++;
            for (int i = 0; i < json.size(); ++i) {
                config.printWhitespace(o);
                serialize(json.getJson(i), config, o);
                if (i < json.size() - 1) {
                    o.append(",");
                }
                o.append(config.eol);
            }
            config.indentCount--;

            if (!isEmpty) {
                config.printWhitespace(o);
            }
            o.append(']');
        }

        private static void serializeS(JsonScalar json,
                InstallmentByteBuffer o) {
            serializeS(json.getString(), o);
        }

        private static void serializeS(String s, InstallmentByteBuffer o) {
            serialize(s, o);
        }
    }

    public static Json build(String src, Producer producer) {
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
        return Serializer.serialize(json, new Config(prefix),
                new InstallmentByteBuffer()).toString();
    }

    public static String serializeAsWellFormed(Json json, String prefix) {
        Config config = new Config();
        config.eol = System.getProperty("line.separator");
        config.margin = prefix;
        config.tab2space = 2;

        return Serializer.serialize(json, config,
                new InstallmentByteBuffer()).toString();
    }

    private Factory() {
        // private dummy
    }
}
