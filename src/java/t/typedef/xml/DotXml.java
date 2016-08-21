package t.typedef.xml;

public class DotXml {

    Config config;

    DeclarationNode head;
    MarkupNode root;

    public StringBuilder toString(StringBuilder o) {
        assert o != null;
        if (root != null) {
            Config c = config.copy();
            if (head != null) {
                head.toString(o, c);
            }
            root.toString(o, c);
        }
        return o;
    }
}
