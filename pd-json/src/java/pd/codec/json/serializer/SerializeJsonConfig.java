package pd.codec.json.serializer;

import pd.fenc.ParsingException;

public class SerializeJsonConfig {

    public String margin = "";
    public String indent = "";
    public String eol = "";
    public String colonPrefix = "";
    public String colonSuffix = "";

    public boolean exportsNull = false;

    public SerializeJsonConfig() {
        this(Style.CHEATSHEET);
    }

    public SerializeJsonConfig(Style style) {
        loadStyle(style);
    }

    public void loadStyle(Style style) {
        switch (style) {
            case CHEATSHEET:
                loadCheatsheetStyle();
                break;
            case MULTILINES:
                loadMultilinesStyle();
                break;
            default:
                throw new ParsingException();
        }
    }

    private void loadCheatsheetStyle() {
        SerializeJsonConfig config = this;
        config.margin = "";
        config.indent = "";
        config.eol = "";
        config.colonPrefix = "";
        config.colonSuffix = "";
    }

    private void loadMultilinesStyle() {
        SerializeJsonConfig config = this;
        config.margin = "";
        config.indent = "  ";
        config.eol = "\n";
        config.colonPrefix = "";
        config.colonSuffix = " ";
    }

    public void setOption(Option optionKey, String value) {
        if (value == null) {
            throw new NullPointerException("value should not be null");
        }
        SerializeJsonConfig config = this;
        switch (optionKey) {
            case MARGIN:
                config.margin = value;
                break;
            case INDENT:
                config.indent = value;
                break;
            case EOL:
                config.eol = value;
                break;
            case COLON_PREFIX:
                config.colonPrefix = value;
                break;
            case COLON_SUFFIX:
                config.colonSuffix = value;
                break;
            default:
                throw new RuntimeException("unknown option: " + optionKey.name());
        }
    }

    public enum Option {
        MARGIN,
        INDENT,
        EOL,
        COLON_PREFIX,
        COLON_SUFFIX
    }

    public enum Style {
        CHEATSHEET,
        MULTILINES
    }
}
