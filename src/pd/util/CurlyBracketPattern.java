package pd.util;

import static pd.fenc.IReader.EOF;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;

import pd.fenc.ParsingException;

/**
 * interpolated string
 * https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/tokens/interpolated
 * https://docs.oracle.com/cd/E19776-01/820-4867/ggqny/index.html
 * https://www.python.org/dev/peps/pep-0498/
 * pattern in c-printf called "format specifiers"
 */
public class CurlyBracketPattern {

    /**
     * use '{}' as formatting anchor
     */
    public static String format(String pattern, Object... args) {
        if (args == null || args.length == 0) {
            return pattern;
        }

        // state machine
        StringBuilder sb = new StringBuilder();
        OfInt it = pattern.codePoints().iterator();
        int nextArgumentIndex = 0;
        int state = 0;
        while (state != 3) {
            switch (state) {
                case 0: {
                    int ch = it.hasNext() ? it.nextInt() : -1;
                    switch (ch) {
                        case '\\':
                            state = 1;
                            break;
                        case '{':
                            state = 2;
                            break;
                        case -1:
                            state = 3;
                            break;
                        default:
                            state = 0;
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
                case 1: {
                    int ch = it.hasNext() ? it.nextInt() : -1;
                    switch (ch) {
                        case '\\':
                            state = 0;
                            sb.append('\\');
                            break;
                        case '{':
                            state = 0;
                            sb.append('{');
                            break;
                        default:
                            String actual = new String(Character.toChars(ch));
                            throw new IllegalArgumentException(String.format("E: unrecognized \"\\%s\"", actual));
                    }
                    break;
                }
                case 2: {
                    int ch = it.hasNext() ? it.nextInt() : -1;
                    switch (ch) {
                        case '}':
                            state = 0;
                            if (nextArgumentIndex < args.length) {
                                sb.append(args[nextArgumentIndex++]);
                            } else {
                                sb.append("{}");
                            }
                            break;
                        default:
                            state = 0;
                            sb.append('{');
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * "a/{cusId}/b({accId})/c{camId}", "a/1/b(2)/c" => { "cusId" = 1, "accId" = 2, "camId" = 3 }
     */
    public static List<Map.Entry<String, String>> match(String pattern, String s) {
//        throw new UnsupportedOperationException();
        // TODO
        return Collections.emptyList();
    }

    /**
     * TODO<br/>
     * - capturing groups cannot be neighbor<br/>
     * - capturing groups cannot have the same name<br/>
     * - capturing group name is a valid identifier<br/>
     */
    public static boolean validate(String pattern) {
        OfInt it = pattern.codePoints().iterator();

        final int STATE_READY = 0;
        final int STATE_READING_KEY = 5;

        // state machine. specials are '{', '}', EOF
        int state = STATE_READY;
        while (true) {
            switch (state) {
                case STATE_READY: {
                    // start point; expects '{', EOF, x
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    if (ch == EOF) {
                        return true;
                    } else if (ch == '{') {
                        state = STATE_READING_KEY;
                    }
                    break;
                }
                case STATE_READING_KEY: {
                    // seen '{', expects '}', x
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    if (ch == EOF) {
                        return false;
                    } else if (ch == '}') {
                        state = STATE_READY;
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    public static void validateOrThrow(String pattern) {
        if (!validate(pattern)) {
            throw new ParsingException("IllegalCurlyBracketPattern: " + pattern);
        }
    }
}
