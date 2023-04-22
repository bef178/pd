package pd.codec.xml;

public class Attribute {

    public String namespacePrefix;

    public String name;

    public String value;

    public boolean isValid() {
        if (namespacePrefix != null && namespacePrefix.isEmpty()) {
            return false;
        }
        if (name == null || name.isEmpty()) {
            return false;
        }
        return true;
    }

    public void serialize(StringBuilder sb) {
        Util.serializeName(namespacePrefix, name, sb);
        if (value != null) {
            sb.append('=').append('\"').append(Util.serializeToQuotedString(value)).append('\"');
        }
    }
}
