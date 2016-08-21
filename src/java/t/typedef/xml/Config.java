package t.typedef.xml;

public class Config {

    public int tabWidth = 4;

    public int indent = 1; // in tabs

    public boolean noTabs = true;

    /**
     * margin-left when l2r of this paragraph
     */
    public transient int margin = 0;

    public Config() {
        // dummy
    }

    public Config(Config config) {
        this.tabWidth = config.tabWidth;
        this.indent = config.indent;
        this.noTabs = config.noTabs;
        this.margin = 0;
    }

    public Config copy() {
        return new Config(this);
    }

    public StringBuilder printIndent(StringBuilder o) {
        assert o != null;
        for (int i = 0; i < indent; ++i) {
            if (noTabs) {
                for (int j = 0; j < tabWidth; ++j) {
                    o.append(' ');
                }
            } else {
                o.append('\t');
            }
        }
        return o;
    }
}
