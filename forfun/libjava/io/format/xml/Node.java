package libjava.io.format.xml;

public abstract class Node {

    enum Type {
        COMMENT, DECLARATION, TAG, TEXT;
    }

    public StringBuilder appendIndent(StringBuilder o, Config c) {
        assert o != null;
        for (int i = 0; i < c.tabsPerIndent; ++i) {
            if (c.usesTabs) {
                o.append('\t');
            } else {
                for (int j = 0; j < c.spacesPerTab; ++j) {
                    o.append(' ');
                }
            }
        }
        return o;
    }

    public abstract StringBuilder toString(StringBuilder o, Config c);
}
