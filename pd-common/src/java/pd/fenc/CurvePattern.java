package pd.fenc;

import static pd.fenc.IReader.EOF;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator.OfInt;
import java.util.Set;

/**
 * interpolated string
 * https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/tokens/interpolated
 * https://docs.oracle.com/cd/E19776-01/820-4867/ggqny/index.html
 * https://www.python.org/dev/peps/pep-0498/
 * pattern in c-printf called "format specifiers"
 */
public class CurvePattern {

    /**
     * use '{}' as formatting anchor
     */
    public static String format(String pattern, Object... args) {
        if (pattern == null) {
            throw new NullPointerException();
        }
        if (args == null || args.length == 0) {
            return pattern;
        }

        StringBuilder sb = new StringBuilder();

        int argsIndex = 0;

        OfInt it = pattern.codePoints().iterator();

        int state = 0;
        while (state != 3) {
            switch (state) {
                case 0: {
                    // ready
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case '\\':
                            state = 1;
                            break;
                        case '{':
                            state = 2;
                            break;
                        case EOF:
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
                    // escaped
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case EOF:
                            throw new ParsingException("E: unexpected EOF");
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
                            throw new ParsingException(String.format("E: unrecognized \"\\%s\"", actual));
                    }
                    break;
                }
                case 2: {
                    // seen '{'
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case '}':
                            state = 0;
                            if (argsIndex < args.length) {
                                sb.append(args[argsIndex++]);
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
     * "a/{cusId}/b({accId})/c{camId}", "a/1/b(2)/c3" => { "cusId": "1", "accId": "2", "camId": "3" }
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
        for (int i = 0; i < cuts.length; i++) {
            if (i % 2 == 0) {
                continue;
            }
            String key = cuts[i];
            String value = s.substring(a[i], a[i + 1]);
            groups.put(key, value);
        }
        return groups;
    }

    /**
     * cut `pattern` into pieces, where odd index being normal String and even index for keys
     */
    private static String[] cutPattern(String pattern) {
        List<String> cuts = new LinkedList<>();

        OfInt it = pattern.codePoints().iterator();
        StringBuilder sb = new StringBuilder();
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    // ready
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case EOF:
                            cuts.add(sb.toString());
                            return cuts.toArray(new String[0]);
                        case '\\':
                            state = 1;
                            break;
                        case '{':
                            state = 2;
                            cuts.add(sb.toString());
                            sb.setLength(0);
                            break;
                        default:
                            sb.appendCodePoint(ch);
                            break;
                    }
                    break;
                }
                case 1: {
                    // escaped
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case EOF:
                            throw new ParsingException("E: unexpected EOF");
                        case '\\':
                            state = 0;
                            sb.append('\\');
                            break;
                        case '{':
                            state = 0;
                            sb.append('{');
                            break;
                        default: {
                            String actual = new String(Character.toChars(ch));
                            throw new ParsingException(String.format("E: unrecognized \"\\%s\"", actual));
                        }
                    }
                    break;
                }
                case 2: {
                    // seen '{'
                    int ch = it.hasNext() ? it.nextInt() : EOF;
                    switch (ch) {
                        case EOF:
                            throw new ParsingException("E: unmatched bracket");
                        case '\\':
                            throw new ParsingException("E: unsupported escaped key");
                        case '}': {
                            state = 0;
                            String key = sb.toString();
                            cuts.add(key);
                            sb.setLength(0);
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
    }

    /**
     * - capturing groups cannot be neighboring<br/>
     * - capturing groups cannot have the same name<br/>
     * - capturing group name is a valid identifier<br/>
     */
    private static void validatePattern(String[] cuts) {
        Set<String> keys = new HashSet<>();
        for (int i = 0; i < cuts.length; i++) {
            String cut = cuts[i];
            if (i % 2 == 0) {
                if (cut.isEmpty() && i != 0 && i != cuts.length - 1) {
                    throw new ParsingException("E: capturing group cannot be contacting");
                }
            } else {
                if (cut.isEmpty() || !cut.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
                    throw new ParsingException("E: capturing group name should be a valid identifier: `" + cut + "`");
                } else if (keys.contains(cut)) {
                    throw new ParsingException("E: capturing group name should be unique: `" + cut + "`");
                } else {
                    keys.add(cut);
                }
            }
        }
    }

    private static boolean fitsPattern(String[] cuts, final int cutsStart, String s, final int sStart, int[] a) {
        assert cutsStart % 2 == 1;

        if (cutsStart == cuts.length - 2) {
            // only one '*' remaining
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
