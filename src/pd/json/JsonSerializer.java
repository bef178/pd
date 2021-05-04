package pd.json;

import java.util.Iterator;
import java.util.Map.Entry;

public class JsonSerializer {

    public String serialize(IJsonToken jsonToken) {
        return serialize(jsonToken, "", "", "", 0);
    }

    public String serialize(IJsonToken jsonToken, String margin, String indent, String eol, int numIndents) {
        StringBuilder sb = new StringBuilder();
        serializeJsonToken(jsonToken, margin, indent, eol, numIndents, sb);
        return sb.toString();
    }

    private void serializeJsonArray(IJsonArray jsonArray, String margin, String indent, String eol, int numIndents,
            StringBuilder sb) {

        if (margin == null) {
            margin = "";
        }

        if (indent == null) {
            indent = "";
        }

        if (eol == null) {
            eol = "";
        }

        sb.append('[');

        if (!jsonArray.isEmpty()) {
            sb.append(eol);
        }

        numIndents++;

        Iterator<IJsonToken> it = jsonArray.iterator();
        while (it.hasNext()) {
            IJsonToken token = it.next();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            serializeJsonToken(token, margin, indent, eol, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(eol);
        }

        numIndents--;

        if (!jsonArray.isEmpty()) {
            serializeMarginAndIndents(margin, indent, numIndents, sb);
        }

        sb.append(']');
    }

    private void serializeJsonTable(IJsonTable jsonTable, String margin, String indent, String eol, int numIndents,
            StringBuilder sb) {

        if (margin == null) {
            margin = "";
        }

        if (indent == null) {
            indent = "";
        }

        if (eol == null) {
            eol = "";
        }

        sb.append('{');

        if (!jsonTable.isEmpty()) {
            sb.append(eol);
        }

        numIndents++;

        Iterator<Entry<String, IJsonToken>> it = jsonTable.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, IJsonToken> entry = it.next();
            String key = entry.getKey();
            IJsonToken value = entry.getValue();

            serializeMarginAndIndents(margin, indent, numIndents, sb);

            sb.append(Util.serializeToQuotedString(key));
            sb.append(':');
            serializeJsonToken(value, margin, indent, eol, numIndents, sb);

            if (it.hasNext()) {
                sb.append(',');
            }

            sb.append(eol);
        }

        numIndents--;

        if (!jsonTable.isEmpty()) {
            serializeMarginAndIndents(margin, indent, numIndents, sb);
        }

        sb.append('}');
    }

    private void serializeJsonToken(IJsonToken jsonToken, String margin, String indent, String eol, int numIndents,
            StringBuilder sb) {
        if (jsonToken instanceof IJsonNull) {
            sb.append("null");
        } else if (jsonToken instanceof IJsonBoolean) {
            sb.append(Boolean.toString(jsonToken.cast(IJsonBoolean.class).value()));
        } else if (jsonToken instanceof IJsonString) {
            sb.append(Util.serializeToQuotedString(jsonToken.cast(IJsonString.class).value()));
        } else if (jsonToken instanceof IJsonArray) {
            serializeJsonArray(jsonToken.cast(IJsonArray.class), margin, indent, eol, numIndents, sb);
        } else if (jsonToken instanceof IJsonTable) {
            serializeJsonTable(jsonToken.cast(IJsonTable.class), margin, indent, eol, numIndents, sb);
        }
    }

    private void serializeMarginAndIndents(String margin, String indent, int numIndents, StringBuilder sb) {
        sb.append(margin);
        for (int i = 0; i < numIndents; i++) {
            sb.append(indent);
        }
    }
}
