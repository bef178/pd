package libcliff.json;

import libcliff.io.InstallmentByteBuffer;

public class Config {

    public String eol = "";

    public String margin = "";

    public int tab2space = 0;

    public transient int indentCount = 0;

    public Config() {
        // default to cheat sheet style
    }

    public Config(String margin) {
        this();
        this.margin = margin;
    }

    public Config(Config config) {
        this.eol = config.eol;
        this.margin = config.margin;
        this.tab2space = config.tab2space;
    }

    public Config copy() {
        return new Config(this);
    }

    public InstallmentByteBuffer printWhitespace(InstallmentByteBuffer o) {
        assert o != null;
        if (margin != null) {
            o.append(margin);
        }
        for (int i = 0; i < indentCount; ++i) {
            if (tab2space >= 0) {
                for (int j = 0; j < tab2space; ++j) {
                    o.append(' ');
                }
            } else {
                o.append('\t');
            }
        }
        return o;
    }
}
