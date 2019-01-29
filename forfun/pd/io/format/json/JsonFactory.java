package pd.io.format.json;

import pd.io.Pullable;
import pd.io.Pushable;
import pd.io.format.FormattingConfig;

public class JsonFactory {

    public static Json parse(Pullable it, JsonProducer producer) {
        return JsonParser.parse(it, producer, Json.class);
    }

    public static <T extends Json> T parse(Pullable it, JsonProducer producer, Class<T> type) {
        return JsonParser.parse(it, producer, type);
    }

    /**
     * cheat sheet style
     */
    public static CharSequence serialize(Json json) {
        return serialize(json, FormattingConfig.getCheatSheet());
    }

    public static CharSequence serialize(Json json, FormattingConfig config) {
        StringBuilder sb = new StringBuilder();
        serialize(json, config, Pushable.wrap(sb));
        return sb;
    }

    public static void serialize(Json json, FormattingConfig config, Pushable it) {
        JsonSerializer.serialize(json, config, it);
    }

    private JsonFactory() {
        // private dummy
    }
}
