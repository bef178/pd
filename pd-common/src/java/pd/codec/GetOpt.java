package pd.codec;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pd.fenc.CurvePattern;

/**
 * This is a restricted limited variant of POSIX getopt. Option configurations are controlled by an option string as well.<br/>
 *
 * Option configurations are separated by comma in option string.<br/>
 * Any option requiring an argument has suffix ":" in its configuration.<br/>
 *
 * Short option configurations match regex {@link GetOpt#shortOptRegex}.<br/>
 * - Options and their arguments could be specified together, or separated by white-spaces, or by "=".<br/>
 * - multiple short options specified together is not supported.<br/>
 *
 * Long option configurations match regex {@link GetOpt#longOptRegex}.<br/>
 * - Options and their arguments could be separated by white-spaces or by "=".<br/>
 *
 * Unknown options are treated as non-option parameters.<br/>
 * Command-line arguments after "--" are treated as non-option parameters.<br/>
 *
 * Output is (String, String) pairs:<br/>
 * - Options with no arguments have `null` value.<br/>
 * - Non-options have key "!opt".<br/>
 * - Order is kept.<br/>
 */
public class GetOpt {

    public static final String shortOptRegex = "(-[A-Za-z0-9])(:?)";
    static final Pattern shortOptRegexPattern = Pattern.compile(shortOptRegex);

    public static final String longOptRegex = "(--[A-Za-z0-9](-?[A-Za-z0-9]+)+)(:?)";
    static final Pattern longOptRegexPattern = Pattern.compile(longOptRegex);

    public static final GetOpt one = new GetOpt();

    public static GetOpt singleton() {
        return one;
    }

    public static List<Map.Entry<String, String>> parse(String optString, String[] args) {
        return new GetOpt(optString).parse(args);
    }

    final LinkedHashMap<String, Boolean> options = new LinkedHashMap<>();

    GetOpt() {
    }

    GetOpt(String optString) {
        opt(optString);
    }

    /**
     * @return this object
     */
    public GetOpt opt(String optString) {
        if (optString == null) {
            throw new IllegalArgumentException();
        }
        for (String optConfig : optString.split(",")) {
            Matcher shortMatcher = shortOptRegexPattern.matcher(optConfig);
            if (shortMatcher.matches()) {
                options.put(shortMatcher.group(1), !shortMatcher.group(2).isEmpty());
                continue;
            }

            Matcher longMatcher = longOptRegexPattern.matcher(optConfig);
            if (longMatcher.matches()) {
                options.put(longMatcher.group(1), !longMatcher.group(3).isEmpty());
                continue;
            }

            throw new IllegalArgumentException(CurvePattern.format("Unrecognized option {}", optConfig));
        }
        return this;
    }

    public List<Map.Entry<String, String>> parse(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException("Input arguments should not be null");
        }

        List<Map.Entry<String, String>> result = new LinkedList<>();

        boolean meetsDashDash = false;
        Iterator<String> it = Arrays.stream(args).iterator();
        while (it.hasNext()) {
            String arg = it.next();
            if (arg == null) {
                throw new IllegalArgumentException("Input arguments should not contain null");
            }

            if (Objects.equals(arg, "--")) {
                meetsDashDash = true;
                continue;
            }

            if (meetsDashDash) {
                result.add(new AbstractMap.SimpleImmutableEntry<>("!opt", arg));
                continue;
            }

            if (options.containsKey(arg)) {
                // e.g. -u User
                boolean optRequiresArg = options.get(arg);
                if (optRequiresArg) {
                    if (it.hasNext()) {
                        result.add(new AbstractMap.SimpleImmutableEntry<>(arg, it.next()));
                    } else {
                        throw new RuntimeException(CurvePattern.format("Option {} requires an argument but there is none", arg));
                    }
                } else {
                    result.add(new AbstractMap.SimpleImmutableEntry<>(arg, null));
                }
                continue;
            }

            int assignmentIndex = arg.indexOf('=');
            if (assignmentIndex >= 2) {
                // e.g. -u=User -Duser=User
                String a0 = arg.substring(0, assignmentIndex);
                if (options.containsKey(a0)) {
                    boolean optRequiresArg = options.get(a0);
                    if (optRequiresArg) {
                        result.add(new AbstractMap.SimpleImmutableEntry<>(a0, arg.substring(assignmentIndex + 1)));
                    } else {
                        throw new RuntimeException(CurvePattern.format("Option {} requires no argument but there is one", a0));
                    }
                    continue;
                }
            }

            if (arg.length() > 2 && arg.charAt(0) == '-') {
                // e.g. -uUser
                String a0 = arg.substring(0, 2);
                if (options.containsKey(a0)) {
                    boolean optRequiresArg = options.get(a0);
                    if (optRequiresArg) {
                        result.add(new AbstractMap.SimpleImmutableEntry<>(a0, arg.substring(2)));
                    } else {
                        throw new RuntimeException(CurvePattern.format("Option {} requires no argument but there is one", a0));
                    }
                    continue;
                }
            }

            result.add(new AbstractMap.SimpleImmutableEntry<>("!opt", arg));
        }

        return result;
    }
}
