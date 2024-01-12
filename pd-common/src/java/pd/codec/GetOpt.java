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

import pd.fenc.CurlyBracketPatternExtension;

/**
 * A restricted limited variant of POSIX getopt, of which option configurations are controlled by an option string as well.<br/>
 * <br/>
 * With the option string,<br/>
 *   - options are separated by comma<br/>
 *   - any option with suffix ":" requires an argument<br/>
 *   - short options match regex {@link GetOpt#shortOptRegex}.<br/>
 *   - long options match regex {@link GetOpt#longOptRegex}.<br/>
 * <br/>
 * With command line arguments,<br/>
 *   - a short option and its argument could be specified together, or separated by white-spaces, or separated by "="<br/>
 *   - multiple short options should not be specified together<br/>
 *   - a long option and its argument could be separated by white-spaces or "="<br/>
 *   - unrecognized options are treated as non-option parameters<br/>
 *   - arguments after "-- " are treated as non-option parameters<br/>
 * <br/>
 * Resulting in a list of (String, String) pairs,<br/>
 *   - values of no-argument-options are all `null`<br/>
 *   - keys of non-options are all `!opt`<br/>
 *   - order of inputs is kept<br/>
 */
public class GetOpt {

    public static final String shortOptRegex = "(-[A-Za-z0-9])(:?)";
    static final Pattern shortOptRegexPattern = Pattern.compile(shortOptRegex);

    public static final String longOptRegex = "(--[A-Za-z0-9]([_-]?[A-Za-z0-9]+)+)(:?)";
    static final Pattern longOptRegexPattern = Pattern.compile(longOptRegex);

    public static List<Map.Entry<String, String>> parse(String optString, String[] args) {
        return new GetOpt(optString).parse(args);
    }

    final LinkedHashMap<String, Boolean> options = new LinkedHashMap<>();

    GetOpt() {
    }

    GetOpt(String optString) {
        opt(optString);
    }

    private void opt(String optString) {
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

            throw new IllegalArgumentException(CurlyBracketPatternExtension.format("Unrecognized option {}", optConfig));
        }
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
                        throw new RuntimeException(CurlyBracketPatternExtension.format("Option {} requires an argument but there is none", arg));
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
                        throw new RuntimeException(CurlyBracketPatternExtension.format("Option {} requires no argument but there is one", a0));
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
                        throw new RuntimeException(CurlyBracketPatternExtension.format("Option {} requires no argument but there is one", a0));
                    }
                    continue;
                }
            }

            result.add(new AbstractMap.SimpleImmutableEntry<>("!opt", arg));
        }

        return result;
    }
}
