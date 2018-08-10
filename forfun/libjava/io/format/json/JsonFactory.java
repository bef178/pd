package libjava.io.format.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import libjava.io.Pullable;
import libjava.io.Pushable;

public class JsonFactory {

    public static class Config {

        public static Config getCheatSheet() {
            return new Config();
        }

        public static Config getWellFormed() {
            Config config = new Config();
            config.EOL = Character.toString('\n'); // System.getProperty("line.separator");
            config.margin = "";
            config.numSpacesPerIndent = 2;
            return config;
        }

        private static void pushCharSequence(CharSequence cs, Pushable pushable) {
            if (cs == null || cs.length() == 0) {
                return;
            }
            OfInt it = cs.codePoints().iterator();
            while (it.hasNext()) {
                pushable.push(it.nextInt());
            }
        }

        public String EOL = null;

        public String margin = null;

        public int numSpacesPerIndent = 0;

        public transient int numIndents = 0;

        public Config() {
            // dummy
        }

        public void pushEol(Pushable pushable) {
            pushCharSequence(EOL, pushable);
        }

        public void pushIndent(Pushable pushable) {
            if (numIndents == 0 || numSpacesPerIndent == 0) {
                return;
            }
            for (int i = 0; i < numIndents; ++i) {
                if (numSpacesPerIndent >= 0) {
                    for (int j = 0; j < numSpacesPerIndent; ++j) {
                        pushable.push(' ');
                    }
                } else {
                    pushable.push('\t');
                }
            }
        }

        public void pushMargin(Pushable pushable) {
            pushCharSequence(margin, pushable);
        }
    }

    static final class JsonSerializer {

        public static void serialize(Json json, Config config, Pushable it) {
            json.serialize(config, it);
        }

        private static void serializeCharSequence(CharSequence cs, Pushable it) {
            it.push('\"');

            OfInt it1 = cs.codePoints().iterator();
            while (it1.hasNext()) {
                int ch = it1.nextInt();
                if (ch == '\"') {
                    it.push('\\');
                    it.push('\"');
                } else {
                    it.push(ch);
                }
            }

            it.push('\"');
        }

        static void serializeObject(JsonObject json, Config config, Pushable it) {

            boolean isEmpty = json.isEmpty();

            it.push('{');
            if (!isEmpty) {
                config.pushEol(it);
            }

            config.numIndents++;

            List<String> keys = new ArrayList<String>(json.keys());
            Collections.sort(keys);
            Iterator<String> keysIt = keys.iterator();
            while (keysIt.hasNext()) {
                String key = keysIt.next();
                Json value = json.getJson(key);

                config.pushMargin(it);
                config.pushIndent(it);

                serializeCharSequence(key, it);
                it.push(':');
                serialize(value, config, it);

                if (keysIt.hasNext()) {
                    it.push(',');
                }
                config.pushEol(it);
            }

            config.numIndents--;

            if (!isEmpty) {
                config.pushMargin(it);
                config.pushIndent(it);
            }

            it.push('}');
        }

        static void serializeScalar(JsonScalar json, Pushable it) {
            serializeCharSequence(json.getString(), it);
        }

        static void serializeVector(JsonVector json, Config config, Pushable it) {

            boolean isEmpty = json.isEmpty();

            it.push('[');
            if (!isEmpty) {
                config.pushEol(it);
            }

            config.numIndents++;

            for (int i = 0; i < json.size(); ++i) {
                config.pushMargin(it);
                config.pushIndent(it);

                serialize(json.getJson(i), config, it);

                if (i < json.size() - 1) {
                    it.push(',');
                }
                config.pushEol(it);
            }

            config.numIndents--;

            if (!isEmpty) {
                config.pushMargin(it);
                config.pushIndent(it);
            }

            it.push(']');
        }

        private JsonSerializer() {
            // private dummy
        }
    }

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
        return serialize(json, Config.getCheatSheet());
    }

    public static CharSequence serialize(Json json, Config config) {
        StringBuilder sb = new StringBuilder();
        serialize(json, config, Pushable.wrap(sb));
        return sb;
    }

    public static void serialize(Json json, Config config, Pushable it) {
        JsonSerializer.serialize(json, config, it);
    }

    private JsonFactory() {
        // private dummy
    }
}
