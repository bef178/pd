package pd.demo.httpechoservice;

import static pd.fenc.IReader.EOF;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pd.fenc.Cascii;
import pd.fenc.CharReader;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;

public class CurlyBracePattern {

    private static final String ERR_ILLEGAL_PATTERN = "Illegal pattern";

    /**
     * charset: visible ascii<br/>
     * "cus/{cusId}/acc({accId})/cam{camId}", "cus/1/acc(2)/cam3" => { "cusId" = 1, "accId" = 2, "camId" = 3 }
     */
    public static List<Map.Entry<String, String>> matchRequestPath(String restPathPattern, String requestPath) {
        List<Map.Entry<String, String>> params = new LinkedList<Map.Entry<String, String>>();
        // TODO 
        return params;
    }

    /**
     * rules:<br/>
     * - charset is visible ascii<br/>
     * - pathPattern is valid path<br/>
     * - one path segment has one or zero capturing group<br/>
     * - capturing group name is valid identifier<br/>
     */
    public static boolean validate(String pathPattern) {
        CharReader it = new CharReader(pathPattern);
        HashSet<String> knownKeys = new HashSet<String>();

        // state machine. specials are '/', '{', '}', EOF
        int state = 0;
        while (true) {
            switch (state) {
                case 0: {
                    // start point 
                    state = 2;
                    break;
                }
                case 1: {
                    // just seen '/', accepts '{', x, EOF
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch == EOF) {
                        return true;
                    } else if (!Cascii.isVisible(ch)) {
                        return false;
                    } else if (ch == '/') {
                        return false;
                    } else if (ch == '{') {
                        state = 3;
                    } else if (ch == '}') {
                        return false;
                    } else {
                        state = 2;
                    }
                    break;
                }
                case 2: {
                    // possible followed by '/', '{', x, EOF
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch == EOF) {
                        return true;
                    } else if (!Cascii.isVisible(ch)) {
                        return false;
                    } else if (ch == '/') {
                        state = 1;
                    } else if (ch == '{') {
                        state = 3;
                    } else if (ch == '}') {
                        return false;
                    } else {
                        // dummy
                        // ordinary ascii, keep the state
                    }
                    break;
                }
                case 3: {
                    // seen '{'
                    try {
                        String key = ScalarPicker.pickIdentifier(it);
                        if (knownKeys.contains(key)) {
                            return false;
                        } else {
                            knownKeys.add(key);
                        }
                    } catch (ParsingException e) {
                        return false;
                    }
                    if (!it.tryEat('{')) {
                        return false;
                    }
                    state = 4;
                    break;
                }
                case 4: {
                    // already seen a capturing group within this segment, accepts '/', x, EOF
                    int ch = it.hasNext() ? it.next() : EOF;
                    if (ch == EOF) {
                        return true;
                    } else if (!Cascii.isVisible(ch)) {
                        return false;
                    } else if (ch == '/') {
                        state = 1;
                    } else if (ch == '{') {
                        return false;
                    } else if (ch == '}') {
                        return false;
                    } else {
                        // dummy, keep the state
                    }
                    break;
                }
                default:
                    throw new IllegalStateException();
            }
        }
    }

    public static void validateOrThrow(String pathPattern) {
        if (!validate(pathPattern)) {
            throw new ParsingException(ERR_ILLEGAL_PATTERN);
        }
    }
}
