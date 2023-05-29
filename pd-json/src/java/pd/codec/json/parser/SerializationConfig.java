package pd.codec.json.parser;

import pd.fenc.ParsingException;

public class SerializationConfig {

    public String margin = "";
    public String indent = "";
    public String eol = "";
    public String colonPrefix = "";
    public String colonSuffix = "";

    public SerializationConfig() {
        this(Style.CHEATSHEET);
    }

    public SerializationConfig(Style style) {
        init(style);
    }

    public void init(Style style) {
        switch (style) {
            case CHEATSHEET:
                mountCheatsheetStyle();
                break;
            case MULTILINES:
                mountMultilinesStyle();
                break;
            default:
                throw new ParsingException();
        }
    }

    private void mountCheatsheetStyle() {
        SerializationConfig config = this;
        config.margin = "";
        config.indent = "";
        config.eol = "";
        config.colonPrefix = "";
        config.colonSuffix = "";
    }

    private void mountMultilinesStyle() {
        SerializationConfig config = this;
        config.margin = "";
        config.indent = "    ";
        config.eol = "\n";
        config.colonPrefix = "";
        config.colonSuffix = " ";
    }

    public void setOption(Option option, String value) {
        if (value == null) {
            throw new NullPointerException("value should not be null");
        }
        SerializationConfig config = this;
        switch (option) {
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
                throw new RuntimeException("unknown option: " + option.name());
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
