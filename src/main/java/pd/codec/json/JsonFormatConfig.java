package pd.codec.json;

class JsonFormatConfig {

    public static JsonFormatConfig cheatsheetStyle() {
        JsonFormatConfig config = new JsonFormatConfig();
        config.mountCheatsheetStyle();
        return config;
    }

    public String margin = "";
    public String indent = "";
    public String eol = "";
    public String colonPrefix = "";
    public String colonSuffix = "";

    public void mountCheatsheetStyle() {
        JsonFormatConfig config = this;
        config.margin = "";
        config.indent = "";
        config.eol = "";
        config.colonPrefix = "";
        config.colonSuffix = "";
    }

    public void mountMultilinesStyle() {
        JsonFormatConfig config = this;
        config.margin = "";
        config.indent = "    ";
        config.eol = "\n";
        config.colonPrefix = "";
        config.colonSuffix = " ";
    }
}
