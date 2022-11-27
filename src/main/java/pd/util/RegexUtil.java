package pd.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil {

    public static List<String> match(String regex, String s) {
        List<String> capturingGroups = new LinkedList<>();
        Matcher m = Pattern.compile(regex).matcher(s);
        if (m.find()) {
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
