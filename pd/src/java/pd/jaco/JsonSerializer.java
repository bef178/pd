package pd.jaco;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;

import lombok.NonNull;
import pd.codec.HexCodec;
import pd.fenc.UnicodeConsumer;
import pd.util.AsciiExtension;

public class JsonSerializer {

    public final Config config;

    public JsonSerializer() {
        this(new Config());
    }

    public JsonSerializer(Config config) {
        this.config = config;
    }

    public String jacoToJson(@NonNull Object o) {
        StringBuilder sb = new StringBuilder();
        jacoToJson(o, 0, UnicodeConsumer.wrap(sb));
        return sb.toString();
    }

    private void jacoToJson(Object o, int numIndents, UnicodeConsumer unicodeConsumer) {
        if (o == null) {
            unicodeConsumer.push("null");
        } else if (o instanceof Map) {
            mapToJson((Map<?, ?>) o, numIndents, unicodeConsumer);
        } else if (o instanceof List) {
            arrayToJson((List<?>) o, numIndents, unicodeConsumer);
        } else if (o.getClass().isArray()) {
            arrayToJson(Arrays.asList((Object[]) o), numIndents, unicodeConsumer);
        } else if (o instanceof String) {
            stringToJson((String) o, unicodeConsumer);
        } else if (o instanceof Number) {
            unicodeConsumer.push(String.valueOf(o));
        } else if (o instanceof Boolean) {
            unicodeConsumer.push(String.valueOf(o));
        } else {
            // XXX reflection get?
            throw JacoException.invalidCollection(o.getClass().getSimpleName());
        }
    }

    private void mapToJson(Map<?, ?> o, int numIndents, UnicodeConsumer unicodeConsumer) {
        unicodeConsumer.push('{');

        if (!o.isEmpty()) {
            unicodeConsumer.push(config.eol);
        }

        numIndents++;

        Iterator<? extends Map.Entry<?, ?>> it = o.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<?, ?> next = it.next();

            outputMarginAndIndents(numIndents, unicodeConsumer);

            stringToJson(next.getKey().toString(), unicodeConsumer);
            unicodeConsumer.push(config.colonPrefix);
            unicodeConsumer.push(':');
            unicodeConsumer.push(config.colonSuffix);
            jacoToJson(next.getValue(), numIndents, unicodeConsumer);

            if (it.hasNext()) {
                unicodeConsumer.push(',');
            }

            unicodeConsumer.push(config.eol);
        }

        numIndents--;

        if (!o.isEmpty()) {
            outputMarginAndIndents(numIndents, unicodeConsumer);
        }

        unicodeConsumer.push('}');
    }

    private void arrayToJson(List<?> o, int numIndents, UnicodeConsumer unicodeConsumer) {
        unicodeConsumer.push('[');

        if (!o.isEmpty()) {
            unicodeConsumer.push(config.eol);
        }

        numIndents++;

        Iterator<?> it = o.iterator();
        while (it.hasNext()) {
            outputMarginAndIndents(numIndents, unicodeConsumer);
            jacoToJson(it.next(), numIndents, unicodeConsumer);
            if (it.hasNext()) {
                unicodeConsumer.push(',');
            }
            unicodeConsumer.push(config.eol);
        }

        numIndents--;

        if (!o.isEmpty()) {
            outputMarginAndIndents(numIndents, unicodeConsumer);
        }

        unicodeConsumer.push(']');
    }

    private void stringToJson(String s, UnicodeConsumer unicodeConsumer) {
        unicodeConsumer.push('\"');
        PrimitiveIterator.OfInt it = s.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            switch (ch) {
                case '\"':
                case '\\':
                    unicodeConsumer.push('\\').push(ch);
                    break;
                case '\b':
                    unicodeConsumer.push('\\').push('b');
                    break;
                case '\f':
                    unicodeConsumer.push('\\').push('f');
                    break;
                case '\n':
                    unicodeConsumer.push('\\').push('n');
                    break;
                case '\r':
                    unicodeConsumer.push('\\').push('r');
                    break;
                case '\t':
                    unicodeConsumer.push('\\').push('t');
                    break;
                default:
                    if (AsciiExtension.isControl(ch)) {
                        int[] a = new int[2];
                        HexCodec.encode1byte((byte) ch, a, 0);
                        unicodeConsumer.push('\\').push('u').push('0').push('0').push((char) a[0]).push((char) a[1]);
                    } else {
                        unicodeConsumer.push(ch);
                    }
                    break;
            }
        }
        unicodeConsumer.push('\"');
    }

    private void outputMarginAndIndents(int numIndents, UnicodeConsumer unicodeConsumer) {
        unicodeConsumer.push(config.margin);
        for (int i = 0; i < numIndents; i++) {
            unicodeConsumer.push(config.indent);
        }
    }

    public static class Config {

        public String margin = "";
        public String indent = "";
        public String eol = "";
        public String colonPrefix = "";
        public String colonSuffix = "";

        public void resetToCheatsheetStyle() {
            Config config = this;
            config.margin = "";
            config.indent = "";
            config.eol = "";
            config.colonPrefix = "";
            config.colonSuffix = "";
        }

        public void resetToPrettyStyle() {
            Config config = this;
            config.margin = "";
            config.indent = "  ";
            config.eol = "\n";
            config.colonPrefix = "";
            config.colonSuffix = " ";
        }
    }
}
