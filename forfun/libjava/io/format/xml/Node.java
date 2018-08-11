package libjava.io.format.xml;

import libjava.io.format.FormattingConfig;

public abstract class Node {

    enum Type {
        COMMENT, DECLARATION, TAG, TEXT;
    }

    public StringBuilder appendIndent(StringBuilder o, FormattingConfig c) {
        assert o != null;
        if (c.usesSpacesInsteadOfTabs) {
            for (int i = 0; i < c.numIndents; ++i) {
                for (int j = 0; j < c.numTabsPerIndent; ++j) {
                    o.append('\t');
                }
            }
        } else {
            for (int i = 0; i < c.numIndents; ++i) {
                for (int j = 0; j < c.numSpacesPerIndent; ++j) {
                    o.append(' ');
                }
            }
        }
        return o;
    }

    public abstract StringBuilder toString(StringBuilder o, FormattingConfig c);
}
