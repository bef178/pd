package libjava.io.format.xml;

public class Config {

    public int spacesPerTab = 4;

    public int tabsPerIndent = 1;

    public boolean usesTabs = false;

    /**
     * margin-left when l2r of this paragraph
     */
    public transient int margin = 0;

    public Config() {
        // dummy
    }

    public Config(Config config) {
        this.spacesPerTab = config.spacesPerTab;
        this.tabsPerIndent = config.tabsPerIndent;
        this.usesTabs = config.usesTabs;
        this.margin = 0;
    }

    public Config copy() {
        return new Config(this);
    }
}
