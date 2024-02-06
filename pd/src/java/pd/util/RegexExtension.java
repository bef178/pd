package pd.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * `string` as regular expression pattern
 */
public class RegexExtension {

    public static List<String> match(String regex, String s) {
        Matcher m = Pattern.compile(regex, Pattern.MULTILINE).matcher(s);
        List<String> capturingGroups = new LinkedList<>();
        while (m.find()) {
            for (int i = 0; i <= m.groupCount(); i++) {
                capturingGroups.add(m.group(i));
            }
        }
        return capturingGroups;
    }

    public static boolean matches(String regex, String s) {
        return s.matches(regex);
    }
}
