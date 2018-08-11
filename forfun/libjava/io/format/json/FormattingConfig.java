package libjava.io.format.json;

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
     * the former part of margin
     */
    public String prefix = null;

    public boolean usesSpacesInsteadOfTabs = true;

    public int numSpacesPerIndent = 4;

    public int numTabsPerIndent = 1;

    /**
     * the latter part of margin
     */
    public transient int numIndents = 0;

    public FormattingConfig() {
        // dummy
    }
}
