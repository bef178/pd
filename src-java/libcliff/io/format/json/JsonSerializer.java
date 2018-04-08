package libcliff.io.format.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import libcliff.io.Pushable;

public class JsonSerializer {

    public static class Config {

        public static Config getCheatSheet() {
            return new Config();
        }

        public static Config getWellFormed(String margin) {
            Config config = new Config();
            config.EOL = Character.toString('\n'); // System.getProperty("line.separator");
            config.margin = margin;
            config.numSpacesPerIndent = 2;
            return config;
        }

        private static int pushCharSequence(CharSequence cs,
                Pushable pushable) {
            if (cs == null || cs.length() == 0) {
                return 0;
            }
            int size = 0;
            OfInt it = cs.codePoints().iterator();
            while (it.hasNext()) {
                size += pushable.push(it.nextInt());
            }
            return size;
        }

        public String EOL = null;

        public String margin = null;

        public int numSpacesPerIndent = 0;

        public transient int numIndents = 0;

        public Config() {
            // dummy
        }

        public int pushEol(Pushable pushable) {
            return pushCharSequence(EOL, pushable);
        }

        public int pushIndent(Pushable pushable) {
            if (numIndents == 0 || numSpacesPerIndent == 0) {
                return 0;
            }
            int size = 0;
            for (int i = 0; i < numIndents; ++i) {
                if (numSpacesPerIndent >= 0) {
                    for (int j = 0; j < numSpacesPerIndent; ++j) {
                        size += pushable.push(' ');
                    }
                } else {
                    size += pushable.push('\t');
                }
            }
            return size;
        }

        public int pushMargin(Pushable pushable) {
            return pushCharSequence(margin, pushable);
        }
    }

    public static CharSequence serialize(Json json) {
        StringBuilder sb = new StringBuilder();
        serialize(json, Pushable.wrap(sb));
        return sb;
    }

    public static CharSequence serialize(Json json, Config config) {
        StringBuilder sb = new StringBuilder();
        serialize(json, config, Pushable.wrap(sb));
        return sb;
    }

    public static int serialize(Json json, Config config, Pushable it) {
        switch (json.type()) {
            case SCALAR:
                return serializeScalar((JsonScalar) json, it);
            case VECTOR:
                return serializeVector((JsonVector) json, config, it);
            case OBJECT:
                return serializeObject((JsonObject) json, config, it);
            default:
                break;
        }
        throw new IllegalJsonTypeException();
    }

    /**
     * cheat sheet style
     */
    public static int serialize(Json json, Pushable it) {
        return serialize(json, Config.getCheatSheet(), it);
    }

    private static int serializeCharSequence(CharSequence cs, Pushable it) {
        int size = it.push('\"');

        OfInt it1 = cs.codePoints().iterator();
        while (it1.hasNext()) {
            int ch = it1.nextInt();
            if (ch == '\"') {
                size += it.push('\\');
                size += it.push('\"');
            } else {
                size += it.push(ch);
            }
        }

        return size + it.push('\"');
    }

    private static int serializeObject(JsonObject json, Config config, Pushable it) {

        boolean isEmpty = json.isEmpty();

        int size = it.push('{');
        if (!isEmpty) {
            size += config.pushEol(it);
        }

        config.numIndents++;

        List<String> keys = new ArrayList<String>(json.keys());
        Collections.sort(keys);
        Iterator<String> keysIt = keys.iterator();
        while (keysIt.hasNext()) {
            String key = keysIt.next();
            Json value = json.getJson(key);

            size += config.pushMargin(it);
            size += config.pushIndent(it);

            size += serializeCharSequence(key, it);
            size += it.push(':');
            size += serialize(value, config, it);

            if (keysIt.hasNext()) {
                size += it.push(',');
            }
            size += config.pushEol(it);
        }

        config.numIndents--;

        if (!isEmpty) {
            size += config.pushMargin(it);
            size += config.pushIndent(it);
        }

        return size + it.push('}');
    }

    private static int serializeScalar(JsonScalar json, Pushable it) {
        return serializeCharSequence(json.getString(), it);
    }

    private static int serializeVector(JsonVector json, Config config, Pushable it) {

        boolean isEmpty = json.isEmpty();

        int size = it.push('[');
        if (!isEmpty) {
            size += config.pushEol(it);
        }

        config.numIndents++;

        for (int i = 0; i < json.size(); ++i) {
            size += config.pushMargin(it);
            size += config.pushIndent(it);

            size += serialize(json.getJson(i), config, it);

            if (i < json.size() - 1) {
                size += it.push(',');
            }
            size += config.pushEol(it);
        }

        config.numIndents--;

        if (!isEmpty) {
            size += config.pushMargin(it);
            size += config.pushIndent(it);
        }

        return size + it.push(']');
    }

    private JsonSerializer() {
        // dummy
    }
}
