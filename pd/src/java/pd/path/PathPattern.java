package pd.path;

import java.util.LinkedList;

import lombok.NonNull;
import pd.fenc.BackableUnicodeProvider;
import pd.fenc.ParsingException;
import pd.fenc.Token;
import pd.util.UnicodeExtension;

import static pd.util.AsciiExtension.EOF;

/**
 * PathPattern:<br/>
 * - `*` matches within boundary `/`, as regex: `[^/]*`<br/>
 * - `**` could across boundary `/`, as regex: `.*`<br/>
 */
public class PathPattern {

    private static final int TOKEN_TYPE_EOF = -1;
    private static final int TOKEN_TYPE_ERROR = -2;
    private static final int TOKEN_TYPE_TEXT = 0;
    private static final int TOKEN_TYPE_ASTERISK = 1;
    private static final int TOKEN_TYPE_DOUBLE_ASTERISK = 2;

    public static boolean matches(String pathPattern, String path) {
        return new PathPattern(pathPattern).matches(path);
    }

    private final String pathPattern;
    private volatile Token[] tokens = null;

    public PathPattern(@NonNull String pathPattern) {
        this.pathPattern = pathPattern;
        this.tokens = tokenize(pathPattern).toArray(new Token[0]);

        Token lastToken = tokens[tokens.length - 1];
        if (lastToken.type == TOKEN_TYPE_ERROR) {
            throw new IllegalArgumentException(lastToken.content);
        }
    }

    public boolean matches(@NonNull String path) {
        if (tokens == null) {
            synchronized (pathPattern) {
                if (tokens == null) {
                    tokens = tokenize(pathPattern).toArray(new Token[0]);
                    Token lastToken = tokens[tokens.length - 1];
                    if (lastToken.type == TOKEN_TYPE_ERROR) {
                        throw new IllegalArgumentException(lastToken.content);
                    }
                }
            }
        }

        return matches(tokens, 0, path.codePoints().mapToObj(UnicodeExtension::toString).toArray(String[]::new), 0);
    }

    private boolean matches(Token[] pathPattern, int pathPatternStart, String[] path, int pathStart) {
        int p = pathPatternStart;
        int s = pathStart;
        while (true) {
            Token token = pathPattern[p];
            switch (token.type) {
                case TOKEN_TYPE_EOF:
                    return s == path.length;
                case TOKEN_TYPE_TEXT: {
                    if (s == path.length || !token.content.equals(path[s])) {
                        return false;
                    }
                    s++;
                    p++;
                    break;
                }
                case TOKEN_TYPE_ASTERISK: {
                    Token nextToken = pathPattern[p + 1];
                    if (nextToken.type == TOKEN_TYPE_EOF) {
                        int s1 = s;
                        while (true) {
                            // consume until EOF or '/'
                            if (s1 == path.length) {
                                return true;
                            }
                            if (path[s1].equals("/")) {
                                return false;
                            }
                            s1++;
                        }
                    } else if (nextToken.type == TOKEN_TYPE_TEXT) {
                        for (int s1 = s; s1 < path.length; s1++) {
                            if (path[s1].equals("/")) {
                                return matches(pathPattern, p + 1, path, s1);
                            } else if (path[s1].equals(nextToken.content)) {
                                if (matches(pathPattern, p + 1, path, s1)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    } else {
                        throw new ParsingException();
                    }
                }
                case TOKEN_TYPE_DOUBLE_ASTERISK: {
                    Token nextToken = pathPattern[p + 1];
                    if (nextToken.type == TOKEN_TYPE_EOF) {
                        return true;
                    } else if (nextToken.type == TOKEN_TYPE_TEXT) {
                        for (int s1 = s; s1 < path.length; s1++) {
                            if (path[s1].equals(nextToken.content)) {
                                if (matches(pathPattern, p + 1, path, s1)) {
                                    return true;
                                }
                            }
                        }
                        return false;
                    } else {
                        throw new ParsingException();
                    }
                }
                default:
                    throw new ParsingException(String.format("PathPattern: unexpected token: %s", token));
            }
        }
    }

    /**
     * provides `tokens`
     * - tokens cannot be null or empty
     * - tokens ends with either TOKEN_TYPE_EOF or TOKEN_TYPE_ERROR
     * - TOKEN_TYPE_EOF / TOKEN_TYPE_ERROR occurs only at the end of tokens, if exists
     */
    private LinkedList<Token> tokenize(String pathPattern) {
        LinkedList<Token> tokens = new LinkedList<>();

        if (pathPattern == null) {
            tokens.add(new Token(TOKEN_TYPE_ERROR, "MalformedPathPattern: null"));
        }

        BackableUnicodeProvider it = new BackableUnicodeProvider(pathPattern);
        while (true) {
            Token token = getNextToken(it);
            if (token.type == TOKEN_TYPE_ASTERISK || token.type == TOKEN_TYPE_DOUBLE_ASTERISK) {
                if (!tokens.isEmpty()) {
                    Token lastToken = tokens.getLast();
                    if (lastToken.type == TOKEN_TYPE_ASTERISK || lastToken.type == TOKEN_TYPE_DOUBLE_ASTERISK) {
                        tokens.add(new Token(TOKEN_TYPE_ERROR, "MalformedPathPattern: unexpected continuous asterisk"));
                        break;
                    }
                }
            }
            tokens.add(token);
            if (token.type == TOKEN_TYPE_EOF || token.type == TOKEN_TYPE_ERROR) {
                break;
            }
        }
        return tokens;
    }

    private Token getNextToken(BackableUnicodeProvider it) {
        final int STATE_READY = 0;
        final int STATE_AFTER_BACK_SLASH = 1;
        final int STATE_AFTER_ASTERISK = 2;

        int state = STATE_READY;
        while (true) {
            switch (state) {
                case STATE_READY: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case EOF:
                            return new Token(TOKEN_TYPE_EOF, null);
                        case '\\':
                            state = STATE_AFTER_BACK_SLASH;
                            break;
                        case '*':
                            state = STATE_AFTER_ASTERISK;
                            break;
                        default:
                            return new Token(TOKEN_TYPE_TEXT, UnicodeExtension.toString(ch));
                    }
                    break;
                }
                case STATE_AFTER_BACK_SLASH: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case '\\':
                            return new Token(TOKEN_TYPE_TEXT, "\\");
                        case '*':
                            return new Token(TOKEN_TYPE_TEXT, "*");
                        default:
                            String message = String.format(
                                    "MalformedPathPattern: unexpected escaped symbol: expecting escaping either '\\' or '*', actual '%s'",
                                    ch == EOF ? "EOF" : UnicodeExtension.toString(ch));
                            return new Token(TOKEN_TYPE_ERROR, message);
                    }
                }
                case STATE_AFTER_ASTERISK: {
                    int ch = it.hasNext() ? it.next() : EOF;
                    switch (ch) {
                        case EOF:
                            return new Token(TOKEN_TYPE_ASTERISK, "*");
                        case '*':
                            return new Token(TOKEN_TYPE_DOUBLE_ASTERISK, "**");
                        default:
                            it.back();
                            return new Token(TOKEN_TYPE_ASTERISK, "*");
                    }
                }
            }
        }
    }
}
