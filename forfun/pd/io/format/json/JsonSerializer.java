package pd.io.format.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.PrimitiveIterator.OfInt;

import pd.io.Pushable;
import pd.io.format.FormattingConfig;

class JsonSerializer {

    private static void pushCharSequence(CharSequence cs, Pushable pushable) {
        if (cs == null || cs.length() == 0) {
            return;
        }
        OfInt it = cs.codePoints().iterator();
        while (it.hasNext()) {
            pushable.push(it.nextInt());
        }
    }

    private static void pushEol(FormattingConfig config, Pushable pushable) {
        pushCharSequence(config.EOL, pushable);
    }

    private static void pushMarginAndIndents(FormattingConfig config, Pushable pushable) {
        pushCharSequence(config.margin, pushable);
        if (config.usesSpacesInsteadOfTabs) {
            for (int i = 0; i < config.numIndents; ++i) {
                for (int j = 0; j < config.numSpacesPerIndent; ++j) {
                    pushable.push(' ');
                }
            }
        } else {
            for (int i = 0; i < config.numIndents; ++i) {
                for (int j = 0; j < config.numTabsPerIndent; ++j) {
                    pushable.push('\t');
                }
            }
        }
    }

    public static void serialize(Json json, FormattingConfig config, Pushable it) {
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

    static void serializeObject(JsonObject json, FormattingConfig config, Pushable it) {

        boolean isEmpty = json.isEmpty();

        it.push('{');
        if (!isEmpty) {
            pushEol(config, it);
        }

        config.numIndents++;

        List<String> keys = new ArrayList<String>(json.keys());
        Collections.sort(keys);
        Iterator<String> keysIt = keys.iterator();
        while (keysIt.hasNext()) {
            String key = keysIt.next();
            Json value = json.getJson(key);

            pushMarginAndIndents(config, it);

            serializeCharSequence(key, it);
            it.push(':');
            serialize(value, config, it);

            if (keysIt.hasNext()) {
                it.push(',');
            }
            pushEol(config, it);
        }

        config.numIndents--;

        if (!isEmpty) {
            pushMarginAndIndents(config, it);
        }

        it.push('}');
    }

    static void serializeScalar(JsonScalar json, Pushable it) {
        serializeCharSequence(json.getString(), it);
    }

    static void serializeVector(JsonVector json, FormattingConfig config, Pushable it) {

        boolean isEmpty = json.isEmpty();

        it.push('[');
        if (!isEmpty) {
            pushEol(config, it);
        }

        config.numIndents++;

        for (int i = 0; i < json.size(); ++i) {
            pushMarginAndIndents(config, it);

            serialize(json.getJson(i), config, it);

            if (i < json.size() - 1) {
                it.push(',');
            }
            pushEol(config, it);
        }

        config.numIndents--;

        if (!isEmpty) {
            pushMarginAndIndents(config, it);
        }

        it.push(']');
    }

    private JsonSerializer() {
        // private dummy
    }
}
