package pd.codec.json.json2object;

import static pd.util.StringExtension.split;

/**
 * It is basically an absolute path separated by '/'.<br/>
 * Array-like's direct element is "[index]".<br/>
 * Map-like's direct element is "{key}".<br/>
 * Patterns:<br/>
 * - `[]` matches any index in array-like<br/>
 * - `{}` matches any key in map-like<br/>
 */
class PathPattern {

    public static final int SCORE_NOT_MATCH = 0;
    public static final int SCORE_BOARD_MATCH = 1;
    // public static final int SCORE_EXACT_MATCH = 2;

    private static final PathPattern one = new PathPattern();

    public static PathPattern singleton() {
        return one;
    }

    public boolean matches(String pathPattern, String path) {
        return score(pathPattern, path) > SCORE_NOT_MATCH;
    }

    public int score(String pathPattern, String path) {
        if (pathPattern == null || path == null) {
            throw new NullPointerException();
        }

        String[] a = split(pathPattern, '/');
        String[] b = split(path, '/');
        if (a.length != b.length) {
            return SCORE_NOT_MATCH;
        }

        int score = SCORE_BOARD_MATCH + 1;
        for (int i = 0; i < a.length; i++) {
            String pSeg = a[i];
            String seg = b[i];
            if (pSeg.equals(seg)) {
                score += 2;
                continue;
            }
            if (pSeg.equals("[]") && seg.charAt(0) == '[' && seg.charAt(seg.length() - 1) == ']') {
                score++;
                continue;
            }
            if (pSeg.equals("{}") && seg.charAt(0) == '{' && seg.charAt(seg.length() - 1) == '}') {
                score++;
                continue;
            }
            return SCORE_NOT_MATCH;
        }
        return score;
    }
}
