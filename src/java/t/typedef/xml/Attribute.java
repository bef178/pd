package t.typedef.xml;

import t.typedef.basic.KeyValue;

public class Attribute extends KeyValue {

    protected String p;

    public Attribute() {
        setNamespaceAbbr("");
        setName("");
        setValue("");
    }

    public String getName() {
        return this.k;
    }

    public String getNamespaceAbbr() {
        return this.p;
    }

    public String getValue() {
        return this.v;
    }

    public Attribute setName(String name) {
        this.k = (name == null) ? "" : name;
        return this;
    }

    public Attribute setNamespaceAbbr(String nsAbbr) {
        this.p = (nsAbbr == null) ? "" : nsAbbr;
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
        if (!getNamespaceAbbr().isEmpty()) {
            factory.append(getNamespaceAbbr()).append(":");
        }
        factory.append(getName()).append('=')
                .append('"').append(getValue()).append('"');
        return factory;
    }
}
