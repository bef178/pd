package libjava.io.format;

public class FormattingConfig {

    public static FormattingConfig getCheatSheet() {
        FormattingConfig config = new FormattingConfig();
        config.EOL = "";
        config.numSpacesPerIndent = 0;
        return config;
    }

    public static FormattingConfig getWellFormed() {
        FormattingConfig config = new FormattingConfig();
        config.EOL = Character.toString('\n'); // System.getProperty("line.separator");
        return config;
    }

    public String EOL = null;

    /**
     * the fixed part
     */
    public String margin = null;

    public boolean usesSpacesInsteadOfTabs = true;

    public int numSpacesPerIndent = 4;

    public int numTabsPerIndent = 1;

    /**
     * the changeable part
     */
    public transient int numIndents = 0;

    public FormattingConfig() {
        // dummy
    }
}
