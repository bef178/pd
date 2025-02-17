package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.fenc.NumberPicker;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;
import pd.fenc.UnicodeProvider;
import pd.util.AsciiExtension;
import pd.util.HexCodec;
import pd.util.SimpleNumber;
import pd.util.UnicodeExtension;

import static pd.util.AsciiExtension.EOF;

public class JacoFromJsonDeserializer {

    private final HexCodec hexCodec = HexCodec.withUpperCaseLetters();

    private final NumberPicker numberPicker = NumberPicker.singleton();

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    public Object fromJson(String s) {
        return jsonToJaco(UnicodeProvider.wrap(s));
    }

    private Object jsonToJaco(UnicodeProvider it) {
        int ch = it.hasNext() ? it.next() : EOF;
        switch (ch) {
            case 'n':
                it.back();
                return jsonToNull(it);
            case 't':
                it.back();
                return jsonToTrue(it);
            case 'f':
                it.back();
                return jsonToFalse(it);
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case '-':
                it.back();
                return jsonToNumber(it);
            case '\"':
                it.back();
                return jsonToString(it);
            case '[':
                it.back();
                return jsonToArray(it);
            case '{':
                it.back();
                return jsonToMap(it);
            default:
                throw new ParsingException();
        }
    }

    private Map<String, Object> jsonToMap(UnicodeProvider it) {
        Map<String, Object> m = new LinkedHashMap<>();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '{') {
                        throw new ParsingException(
                                String.format("expected '{', actual [%s]", UnicodeExtension.toString(ch)));
                    }
                    scalarPicker.eatWhitespaces(it);
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '}':
                            return m;
                        default:
                            it.back();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // deserializeJsonKeyValue()
                    String pKey = jsonToString(it);
                    scalarPicker.eatWhitespaces(it);
                    scalarPicker.eatOneOrThrow(it, ':');
                    scalarPicker.eatWhitespaces(it);
                    Object pValue = jsonToJaco(it);

                    m.put(pKey, pValue);
                    scalarPicker.eatWhitespaces(it);
                    state = 3;
                    break;
                }
                case 3: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '}':
                            return m;
                        case ',':
                            scalarPicker.eatWhitespaces(it);
                            state = 2;
                            break;
                        default:
                            throw new ParsingException(
                                    String.format("expected '}' or ',', actual [%s]", UnicodeExtension.toString(ch)));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private List<Object> jsonToArray(UnicodeProvider it) {
        List<Object> a = new LinkedList<>();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '[') {
                        throw new ParsingException(
                                String.format("expected '[', actual [%s]", UnicodeExtension.toString(ch)));
                    }
                    scalarPicker.eatWhitespaces(it);
                    state = 1;
                    break;
                }
                case 1: {
                    // a json or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return a;
                        default:
                            it.back();
                            state = 2;
                            break;
                    }
                    break;
                }
                case 2: {
                    // a json
                    a.add(jsonToJaco(it));
                    scalarPicker.eatWhitespaces(it);
                    state = 3;
                    break;
                }
                case 3: {
                    // a comma or the end
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case ']':
                            return a;
                        case ',':
                            scalarPicker.eatWhitespaces(it);
                            state = 2;
                            break;
                        default:
                            throw new ParsingException(
                                    String.format("expected ']' or ',', actual [%s]", UnicodeExtension.toString(ch)));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private String jsonToString(UnicodeProvider it) {
        // state machine go!
        int state = 0;
        StringBuilder sb = new StringBuilder();
        while (true) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch != '\"') {
                        throw new ParsingException(
                                String.format("expected '\"', actual [%s]", UnicodeExtension.toString(ch)));
                    }
                    state = 1;
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch == '\"') {
                        // consume the end delimiter and exit
                        return sb.toString();
                    } else if (ch == '\\') {
                        state = 2;
                    } else if (AsciiExtension.isControl(ch)) {
                        throw new ParsingException(String.format("unexpected unicode(%d)", ch));
                    } else {
                        sb.appendCodePoint(ch);
                    }
                    break;
                }
                case 2: {
                    // escaping
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '\"':
                            state = 1;
                            sb.append('\"');
                            break;
                        case '\\':
                            state = 1;
                            sb.append('\\');
                            break;
                        case '/':
                            state = 1;
                            sb.append('/');
                            break;
                        case 'b':
                            state = 1;
                            sb.append('\b');
                            break;
                        case 'f':
                            state = 1;
                            sb.append('\f');
                            break;
                        case 'n':
                            state = 1;
                            sb.append('\n');
                            break;
                        case 'r':
                            state = 1;
                            sb.append('\r');
                            break;
                        case 't':
                            state = 1;
                            sb.append('\t');
                            break;
                        case 'u':
                            int[] u = new int[4];
                            u[0] = it.next();
                            u[1] = it.next();
                            u[2] = it.next();
                            u[3] = it.next();
                            sb.append((char) ((hexCodec.decode(u[0], u[1]) << 8) | (hexCodec.decode(u[2], u[3]) & 0xFF)));
                            state = 1;
                            break;
                        default:
                            throw new ParsingException(String.format("unexpected [\\%c]", ch));
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    private Number jsonToNumber(UnicodeProvider it) {
        String floatToken = numberPicker.pickFloatToken(it);
        SimpleNumber number = new SimpleNumber(floatToken);
        if (number.isRoundNumber()) {
            return number.getInt64();
        } else {
            return number.getFloat64();
        }
    }

    private Boolean jsonToTrue(UnicodeProvider src) {
        scalarPicker.eatSequenceOrThrow(src, "true");
        return true;
    }

    private Boolean jsonToFalse(UnicodeProvider it) {
        scalarPicker.eatSequenceOrThrow(it, "false");
        return false;
    }

    private Object jsonToNull(UnicodeProvider it) {
        scalarPicker.eatSequenceOrThrow(it, "null");
        return null;
    }
}
