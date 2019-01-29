package pd.io.format.xml;

public class Attribute {

    protected String namespace;

    protected String name;

    protected String value;

    public Attribute() {
        namespace = "";
        name = "";
        value = "";
    }

    public String getName() {
        return this.name;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getValue() {
        return this.value;
    }

    public boolean isValid() {
        return name != null && !name.isEmpty();
    }

    public Attribute setName(String name) {
        this.name = (name == null) ? "" : name;
        return this;
    }

    public Attribute setNamespace(String namespace) {
        this.namespace = (namespace == null) ? "" : namespace;
        return this;
    }

    public Attribute setValue(String value) {
        this.value = (value == null) ? "" : value;
        return this;
    }

    public StringBuilder toString(StringBuilder o) {
        assert o != null;
        if (!isValid()) {
            throw new IllegalArgumentException();
        }
        if (!namespace.isEmpty()) {
            o.append(namespace).append(":");
        }
        o.append(name).append('=').append('"').append(value).append('"');
        return o;
    }
}
