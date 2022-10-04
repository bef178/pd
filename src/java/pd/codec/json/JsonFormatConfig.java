package pd.codec.json;

public class JsonFormatConfig {

    public static JsonFormatConfig cheetsheetStyle() {
        JsonFormatConfig config = new JsonFormatConfig();
        config.margin = "";
        config.indent = "";
        config.eol = "";
        config.colonPrefix = "";
        config.colonSuffix = "";
        return config;
    }

    public static JsonFormatConfig multilinesSytle() {
        JsonFormatConfig config = new JsonFormatConfig();
        config.margin = "";
        config.indent = "    ";
        config.eol = "\n";
        config.colonPrefix = "";
        config.colonSuffix = " ";
        return config;
    }

    public String margin = "";
    public String indent = "";
    public String eol = "";
    public String colonPrefix = "";
    public String colonSuffix = "";
}
