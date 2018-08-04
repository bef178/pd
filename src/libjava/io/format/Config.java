package libjava.io.format;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import libjava.io.ParsingException;
import libjava.io.Pullable;

public class Config {

    public static Entry<String, String> fromConfigLine(Feeder puller) {
        int ch = Parser.eatWhitespace(puller);
        if (ch == '#') {
            // comment line
            return new SimpleImmutableEntry<String, String>(null, null);
        }

        String id = Parser.pickDottedIdentifier(puller);

        ch = Parser.eatWhitespace(puller);
        if (ch != '=') {
            throw new ParsingException();
        }

        ch = Parser.eatWhitespace(puller);
        if (ch == '\"') {
            String value = Parser.pickString(puller, '\"');
            ch = Parser.eatWhitespace(puller);
            if (ch != Pullable.EOF) {
                throw new ParsingException();
            } else {
                return new SimpleImmutableEntry<String, String>(id, value);
            }
        } else {
            String value = Parser.pickString(puller, Pullable.EOF);
            return new SimpleImmutableEntry<String, String>(id, value);
        }
    }
}
