package pd.json.serializer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.PrimitiveIterator.OfInt;

import pd.codec.HexCodec;
import pd.fenc.ParsingException;
import pd.json.datatype.Json;
import pd.json.datatype.JsonArray;
import pd.json.datatype.JsonBoolean;
import pd.json.datatype.JsonNull;
import pd.json.datatype.JsonNumber;
import pd.json.datatype.JsonObject;
import pd.json.datatype.JsonString;
import pd.util.AsciiExtension;

public class Serializer {

    private final SerializingConfig config;

    public Serializer(SerializingConfig config) {
        this.config = config;
    }

    /**
     * `Json` => `String`
     */
    public String serialize(Json json) {
        StringBuilder sb = new StringBuilder();
        serializeJson(json, 0, sb);
        return sb.toString();
    }

    private void serializeJson(Json json, int numIndents, StringBuilder sb) {
        if (json instanceof JsonNull) {
            sb.append("null");
        } else if (json instanceof JsonBoolean) {
            sb.append(((JsonBoolean) json).getBoolean());
        } else if (json instanceof JsonNumber) {
            sb.append(json);
        } else if (json instanceof JsonString) {
            serializeJsonString(((JsonString) json).getString(), sb);
        } else if (json instanceof JsonArray) {
            serializeJsonArray((JsonArray) json, numIndents, sb);
        } else if (json instanceof JsonObject) {
            serializeJsonObject((JsonObject) json, numIndents, sb);
        } else {
            throw new ParsingException();
        }
    }

    private void serializeJsonArray(JsonArray jsonArray, int numIndents, StringBuilder sb) {

        sb.append('[');

        if (!jsonArray.isEmpty()) {
            sb.append(config.eol);
        }

        numIndents++;

        Iterator<Json> it = jsonArray.iterator();
        while (it.hasNext()) {
            serializeMarginAndIndents(numIndents, sb);
            serializeJson(it.next(), numIndents, sb);
            if (it.hasNext()) {
                sb.append(',');
            }
            sb.append(config.eol);
        }

        numIndents--;

        if (!jsonArray.isEmpty()) {
            serializeMarginAndIndents(numIndents, sb);
        }

        sb.append(']');
    }

    private void serializeJsonObject(JsonObject jsonObject, int numIndents, StringBuilder sb) {

        sb.append('{');

        if (!jsonObject.isEmpty()) {
            sb.append(config.eol);
        }

        numIndents++;

        List<Entry<String, Json>> l = new LinkedList<>(jsonObject.entrySet());
        l.removeIf(a -> a.getValue() == null || a.getValue() instanceof JsonNull && !config.exportsNull);
        Iterator<Entry<String, Json>> it = l.iterator();
        while (it.hasNext()) {
            Entry<String, Json> entry = it.next();
            String key = entry.getKey();
            Json value = entry.getValue();

            serializeMarginAndIndents(numIndents, sb);

            serializeJsonString(key, sb);
            sb.append(config.colonPrefix);
            sb.append(':');
            sb.append(config.colonSuffix);
            serializeJson(value, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(config.eol);
        }

        numIndents--;

        if (!jsonObject.isEmpty()) {
            serializeMarginAndIndents(numIndents, sb);
        }

        sb.append('}');
    }

    private void serializeJsonString(String s, StringBuilder sb) {
        sb.appendCodePoint('\"');
        OfInt it = s.codePoints().iterator();
        while (it.hasNext()) {
            int ch = it.nextInt();
            switch (ch) {
                case '\"':
                case '\\':
                    sb.append('\\').append(ch);
                    break;
                case '\b':
                    sb.append('\\').append('b');
                    break;
                case '\f':
                    sb.append('\\').append('f');
                    break;
                case '\n':
                    sb.append('\\').append('n');
                    break;
                case '\r':
                    sb.append('\\').append('r');
                    break;
                case '\t':
                    sb.append('\\').append('t');
                    break;
                default:
                    if (AsciiExtension.isControl(ch)) {
                        int[] a = new int[2];
                        HexCodec.encode1byte((byte) ch, a, 0);
                        sb.append('\\').append('u').append('0').append('0').append((char) a[0]).append((char) a[1]);
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
            }
        }
        sb.appendCodePoint('\"');
    }

    private void serializeMarginAndIndents(int numIndents, StringBuilder sb) {
        sb.append(config.margin);
        for (int i = 0; i < numIndents; i++) {
            sb.append(config.indent);
        }
    }
}
