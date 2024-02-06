package pd.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;

import pd.util.UnicodeExtension;

/**
 * interpolated string
 * https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/tokens/interpolated<br/>
 * https://docs.oracle.com/cd/E19776-01/820-4867/ggqny/index.html<br/>
 * https://www.python.org/dev/peps/pep-0498/<br/>
 * pattern in c-printf called "format specifiers"<br/>
 */
public class CurlyBracketPatternExtension {

    private static final int EOF = -1;

    private static final int STATE_READY = 0;
    private static final int STATE_ON_BACK_SLASH = 1;
    private static final int STATE_ON_OPENING_CURLY_BRACKET = 2;

    /**
     * use '{}' as formatting anchor
     */
    public static String format(String pattern, Object... args) {
        if (args == null || args.length == 0) {
            return pattern;
        }

        int argsIndex = 0;
        OfInt it = pattern.codePoints().iterator();
        StringBuilder sb = new StringBuilder();
        int state = STATE_READY;
        int limit = 50000;
        while (limit-- > 0) {
            switch (state) {
                case STATE_READY: {
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case EOF:
                            return sb.toString();
                        case '\\':
                            state = STATE_ON_BACK_SLASH;
                            break;
                        case '{':
                            state = STATE_ON_OPENING_CURLY_BRACKET;
                            break;
                        default:
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
                case STATE_ON_BACK_SLASH: {
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case '\\':
                        case '{':
                            sb.appendCodePoint(ch);
                            state = STATE_READY;
                            break;
                        default:
                            throw new IllegalStateException(String.format("E: unexpected token '%s' after '\\'", UnicodeExtension.toString(ch)));
                    }
                    break;
                }
                case STATE_ON_OPENING_CURLY_BRACKET: {
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case '}': {
                            if (argsIndex < args.length) {
                                sb.append(args[argsIndex++]);
                            } else {
                                sb.append("{}");
                            }
                            break;
                        }
                        default:
                            throw new IllegalStateException(String.format("E: unexpected token '%s' after '{'", UnicodeExtension.toString(ch)));
                    }
                    state = STATE_READY;
                    break;
                }
            }
        }
        throw new IllegalStateException("E: reached loop limit");
    }

    /**
     * "a/{category}/b({type})/c{item}", "a/1/b(2)/c3" => { "category": "1", "type": "2", "item": "3" }
     */
    public static Map<String, String> match(String pattern, String s) {
        String[] cuts = cutPattern(pattern);
        validatePattern(cuts);

        if (!s.startsWith(cuts[0]) || !s.endsWith(cuts[cuts.length - 1])) {
            return null;
        }

        if (cuts.length == 1) {
            return Collections.emptyMap();
        }

        int[] a = new int[cuts.length];
        a[0] = 0;
        a[a.length - 1] = s.length() - cuts[cuts.length - 1].length();

        if (!fitsPattern(cuts, 1, s, cuts[0].length(), a)) {
            return null;
        }

        Map<String, String> groups = new LinkedHashMap<>();
        for (int i = 1; i < cuts.length; i += 2) {
            String key = cuts[i];
            String value = s.substring(a[i], a[i + 1]);
            groups.put(key, value);
        }
        return groups;
    }

    /**
     * cut `pattern` into pieces, where odd index being normal String and even index being key
     */
    private static String[] cutPattern(String pattern) {
        List<String> cuts = new LinkedList<>();

        OfInt it = pattern.codePoints().iterator();
        StringBuilder sb = new StringBuilder();
        int state = STATE_READY;
        int limit = 50000;
        while (limit-- > 0) {
            switch (state) {
                case STATE_READY: {
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case EOF:
                            cuts.add(sb.toString());
                            return cuts.toArray(new String[0]);
                        case '\\':
                            state = STATE_ON_BACK_SLASH;
                            break;
                        case '{':
                            cuts.add(sb.toString());
                            sb.setLength(0);
                            state = STATE_ON_OPENING_CURLY_BRACKET;
                            break;
                        default:
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
                case STATE_ON_BACK_SLASH: {
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case '\\':
                        case '{':
                            sb.appendCodePoint(ch);
                            state = STATE_READY;
                            break;
                        default:
                            throw new IllegalStateException(String.format("E: unexpected token '%s' after '\\'", UnicodeExtension.toString(ch)));
                    }
                    break;
                }
                case STATE_ON_OPENING_CURLY_BRACKET: {
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case EOF:
                        case '\\':
                            throw new IllegalStateException(String.format("E: unexpected token '%s' after '{'", UnicodeExtension.toString(ch)));
                        case '}': {
                            String key = sb.toString();
                            cuts.add(key);
                            sb.setLength(0);
                            state = STATE_READY;
                            break;
                        }
                        default:
                            sb.appendCodePoint(ch);
                            break;
                    }
                }
                default:
                    break;
            }
        }
        throw new IllegalStateException("E: reached loop limit");
    }

    /**
     * - capturing groups cannot be neighboring<br/>
     * - capturing group name should be a valid identifier<br/>
     * - capturing group name should be unique<br/>
     */
    private static void validatePattern(String[] cuts) {
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < cuts.length; i++) {
            String cut = cuts[i];
            if (i % 2 == 0) {
                if (cut.isEmpty() && i != 0 && i != cuts.length - 1) {
                    throw new IllegalArgumentException("E: capturing groups cannot be neighboring");
                }
            } else {
                if (cut.isEmpty() || !cut.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                    throw new IllegalArgumentException("E: capturing group name should be a valid identifier: `" + cut + "`");
                } else if (keys.contains(cut)) {
                    throw new IllegalArgumentException("E: capturing group name should be unique: `" + cut + "`");
                } else {
                    keys.add(cut);
                }
            }
        }
    }

    private static boolean fitsPattern(String[] cuts, final int cutsStart, String s, final int sStart, int[] a) {
        assert cutsStart % 2 == 1;

        if (cutsStart == cuts.length - 2) {
            // only one '{*}' remaining
            a[cutsStart] = sStart;
            return true;
        }

        String next = cuts[cutsStart + 1];
        int nextStart = sStart;
        while (true) {
            nextStart = s.indexOf(next, nextStart);
            if (nextStart == -1 || nextStart + next.length() > a[cuts.length - 1]) {
                return false;
            }
            if (fitsPattern(cuts, cutsStart + 2, s, nextStart + next.length(), a)) {
                a[cutsStart] = sStart;
                a[cutsStart + 1] = nextStart;
                return true;
            }
            nextStart++;
        }
    }
}
