package t.typedef.xml;

public class Tag extends Attribute {

    @Override
    public boolean isValid() {
        return getValue().isEmpty() && super.isValid();
    }

    @Override
    public StringBuilder toString(StringBuilder factory) {
        assert factory != null;
        if (!isValid()) {
            throw new IllegalArgumentException();
        }
        if (!getNamespaceAbbr().isEmpty()) {
            factory.append(getNamespaceAbbr()).append(":");
        }
        factory.append(getName());
        return factory;
    }
}
