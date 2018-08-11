package libjava.io.format.xml;

import libjava.io.format.FormattingConfig;

public class Document {

    DeclarationNode header;

    MarkupNode root;

    public StringBuilder toString(StringBuilder o, FormattingConfig c) {
        assert o != null;
        if (root != null) {
            if (header != null) {
                header.toString(o, c);
            }
            root.toString(o, c);
        }
        return o;
    }
}
