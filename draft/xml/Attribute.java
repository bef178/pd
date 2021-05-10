package pd.fenc.xml;

import pd.fenc.Util;

public class Attribute {

    public String nsprefix;

    public String key;

    public String value;

    public boolean isValid() {
        if (nsprefix != null && nsprefix.isEmpty()) {
            return false;
        }
        if (key == null || key.isEmpty()) {
            return false;
        }
        return true;
    }

    public void serialize(StringBuilder sb) {
        if (nsprefix != null) {
            sb.append(nsprefix).append(':');
        }
        sb.append(key);
        if (value != null) {
            sb.append('=').append('\"').append(Util.serializeToQuotedString(value)).append('\"');
        }
    }
}
