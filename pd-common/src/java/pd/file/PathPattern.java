package pd.file;

/**
 * pattern:<br/>
 * - `*` matches within a segment of path<br/>
 * as regex: <code>[^/]*</code><br/>
 * // *   - `**` matches a sequence of segments of path<br/>
 * // *     as regex: <code>.*</code><br/>
 */
public class PathPattern {

    private static final PathPattern one = new PathPattern();

    public static PathPattern singleton() {
        return one;
    }

    public boolean matches(String pattern, String path) {
        return matches(pattern, 0, path, 0);
    }

    private boolean matches(String pattern, int patternStart, String path, int pathStart) {
        if (pattern == null || path == null) {
            throw new IllegalArgumentException();
        }

        final int TOKEN_EOF = -1;
        final int TOKEN_ASTERISK = -11;
        int token;

        boolean inAsteriskMatching = false;

        int p = patternStart;
        int s = pathStart;
        while (true) {
            // read next token
            if (p == pattern.length()) {
                token = TOKEN_EOF;
            } else {
                int ch = pattern.charAt(p++);
                if (ch == '*') {
                    token = TOKEN_ASTERISK;
                } else if (ch == '\\') {
                    // escaped. look at next symbol
                    if (p == pattern.length()) {
                        throw new IllegalArgumentException("Malformed pattern: only allows to escape either '\\' or '*'");
                    }
                    int ch1 = pattern.charAt(p++);
                    if (ch1 == '\\') {
                        token = '\\';
                    } else if (ch1 == '*') {
                        token = '*';
                    } else {
                        throw new IllegalArgumentException("Malformed pattern: only allows to escape either '\\' or '*'");
                    }
                } else {
                    token = ch;
                }
            }

            if (token == TOKEN_EOF) {
                if (inAsteriskMatching) {
                    for (int i = s; i < path.length(); i++) {
                        if (path.charAt(i) == '/') {
                            return false;
                        }
                    }
                    return true;
                }
                return s == path.length();
            } else if (token == TOKEN_ASTERISK) {
                if (inAsteriskMatching) {
                    throw new IllegalArgumentException("Malformed pattern: illegal continuous asterisk");
                }
                inAsteriskMatching = true;
            } else {
                if (inAsteriskMatching) {
                    for (int i = s; i < path.length(); i++) {
                        int ch = path.charAt(i);
                        if (ch == token) {
                            if (matches(pattern, p, path, i + 1)) {
                                return true;
                            }
                            break;
                        } else if (ch == '/') {
                            break;
                        }
                    }
                    return false;
                }
                if (token != path.charAt(s++)) {
                    return false;
                }
            }
        }
    }
}
