package t.typedef.xml;

public class Namespace extends Attribute {

    @Override
    public boolean isValid() {
        return getNamespaceAbbr().equals("xmlns") && super.isValid();
    }
}
