package t.typedef.xml;

import t.typedef.basic.KeyValue;

public class Attribute extends KeyValue {

    protected String p;

    public Attribute() {
        setNamespace("");
        setName("");
        setValue("");
    }

    public String getName() {
        return this.k;
    }

    public String getNamespace() {
        return this.p;
    }

    public String getValue() {
        return this.v;
    }

    public Attribute setName(String name) {
        this.k = (name == null) ? "" : name;
        return this;
    }

    public Attribute setNamespace(String abbr) {
        this.p = (abbr == null) ? "" : abbr;
        return this;
    }

    public Attribute setValue(String value) {
        this.v = (value == null) ? "" : value;
        return this;
    }

    public StringBuilder toString(StringBuilder factory) {
        assert factory != null;
        if (!isValid()) {
            throw new IllegalArgumentException();
        }
        if (!getNamespace().isEmpty()) {
            factory.append(getNamespace()).append(":");
        }
        factory.append(getName()).append('=')
                .append('"').append(getValue()).append('"');
        return factory;
    }
}
