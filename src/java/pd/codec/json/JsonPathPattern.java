package pd.codec.json;

import static pd.util.StringExtension.split;

/**
 * It is basically an absolute path separated by '/'.<br/>
 * Array-like's direct element is "[index]".<br/>
 * Map-like's direct element is "{key}".<br/>
 * Patterns:<br/>
 * - `[]` matches any index in array-like<br/>
 * - `{}` matches any key in map-like<br/>
 */
class JsonPathPattern {

    public static final int SCORE_NOT_MATCH = -1;
    public static final int SCORE_EXACT_MATCH = 0;
    public static final int SCORE_BOARD_MATCH = 999;

    public static int matches(String pattern, String path) {
        assert pattern != null && !pattern.isEmpty();
        assert path != null;

        String[] patternSegments = split(pattern, '/');
        String[] pathSegements = split(path, '/');
        if (patternSegments.length != pathSegements.length) {
            return SCORE_NOT_MATCH;
        }

        int score = 0;
        for (int i = 0; i < patternSegments.length; i++) {
            String pSeg = patternSegments[i];
            String seg = pathSegements[i];
            if (pSeg.equals(seg)) {
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
