package t.typedef.xml;

public class DefaultNamespace extends Attribute {

    @Override
    public boolean isValid() {
        return getNamespaceAbbr().isEmpty()
                && getName().equals("xmlns")
                && !getValue().isEmpty();
    }
}
