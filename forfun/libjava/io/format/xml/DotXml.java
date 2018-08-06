package libjava.io.format.xml;

public class DotXml {

    Config config;

    DeclarationNode header;

    MarkupNode root;

    public StringBuilder toString(StringBuilder o) {
        assert o != null;
        if (root != null) {
            Config c = config.copy();
            if (header != null) {
                header.toString(o, c);
            }
            root.toString(o, c);
        }
        return o;
    }
}
