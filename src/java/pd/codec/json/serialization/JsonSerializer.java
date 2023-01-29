package pd.codec.json.serialization;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.PrimitiveIterator.OfInt;

import pd.codec.HexCodec;
import pd.codec.json.IJson;
import pd.codec.json.IJsonArray;
import pd.codec.json.IJsonBoolean;
import pd.codec.json.IJsonNull;
import pd.codec.json.IJsonNumber;
import pd.codec.json.IJsonObject;
import pd.codec.json.IJsonString;
import pd.fenc.ParsingException;
import pd.util.AsciiUtil;

public class JsonSerializer {

    private final SerializationConfig config;

    public JsonSerializer(SerializationConfig config) {
        this.config = config;
    }

    /**
     * `IJson` => `String`<br/>
     */
    public String serialize(IJson json) {
        StringBuilder sb = new StringBuilder();
        serializeJson(json, 0, sb);
        return sb.toString();
    }

    private void serializeJson(IJson json, int numIndents, StringBuilder sb) {
        if (json instanceof IJsonNull) {
            sb.append("null");
        } else if (json instanceof IJsonBoolean) {
            sb.append(IJsonBoolean.class.cast(json).getBoolean());
        } else if (json instanceof IJsonNumber) {
            sb.append(IJsonNumber.class.cast(json));
        } else if (json instanceof IJsonString) {
            serializeJsonString(IJsonString.class.cast(json).getString(), sb);
        } else if (json instanceof IJsonArray) {
            serializeJsonArray(IJsonArray.class.cast(json), numIndents, sb);
        } else if (json instanceof IJsonObject) {
            serializeJsonObject(IJsonObject.class.cast(json), numIndents, sb);
        } else {
            throw new ParsingException();
        }
    }

    private void serializeJsonArray(IJsonArray jsonArray, int numIndents, StringBuilder sb) {

        sb.append('[');

        if (!jsonArray.isEmpty()) {
            sb.append(config.eol);
        }

        numIndents++;

        Iterator<IJson> it = jsonArray.iterator();
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

    private void serializeJsonObject(IJsonObject jsonObject, int numIndents, StringBuilder sb) {

        sb.append('{');

        if (!jsonObject.isEmpty()) {
            sb.append(config.eol);
        }

        numIndents++;

        Iterator<Entry<String, IJson>> it = jsonObject.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, IJson> entry = it.next();
            String key = entry.getKey();
            IJson value = entry.getValue();

            if (value == null) {
                continue;
            }

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
                    if (AsciiUtil.isControl(ch)) {
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
