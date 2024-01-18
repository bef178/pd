package pd.util;

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
 * - options are separated by comma<br/>
 * - any option with suffix ":" requires an argument<br/>
 * - single-alphanumeric options match regex {@link GetOpt#singleAlphanumericOptRegex}.<br/>
 * - long options match regex {@link GetOpt#longOptRegex}.<br/>
 * <br/>
 * With command line arguments,<br/>
 * - a single-alphanumeric option and its argument could be specified together or separated by whitespaces<br/>
 * - multiple single-alphanumeric options should not be specified together<br/>
 * - a long option and its argument could be separated by whitespaces or "="<br/>
 * - unrecognized options will be treated as non-option parameters<br/>
 * - arguments after " -- " will not be parsed and be treated as non-option parameters<br/>
 * <br/>
 * Resulting in a list of (String, String) pairs,<br/>
 * - values of no-argument-options are all `null`<br/>
 * - keys of non-options are all `!opt`<br/>
 * - order of inputs is kept<br/>
 */
public class GetOpt {

    public static final String NON_OPT_KEY = "!opt";

    public static final String singleAlphanumericOptRegex = "(-[A-Za-z0-9])(:?)";
    static final Pattern singleAlphanumericOptRegexPattern = Pattern.compile(singleAlphanumericOptRegex);

    public static final String longOptRegex = "(--[A-Za-z0-9]([_-]?[A-Za-z0-9]+)+)(:?)";
    static final Pattern longOptRegexPattern = Pattern.compile(longOptRegex);

    public static List<Map.Entry<String, String>> parse(String optString, String[] args) {
        return new GetOpt().opt(optString).parse(args);
    }

    final LinkedHashMap<String, Boolean> options = new LinkedHashMap<>();

    public GetOpt opt(String optString) {
        if (optString == null || optString.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (String s : optString.split(",")) {
            {
                Matcher matcher = singleAlphanumericOptRegexPattern.matcher(s);
                if (matcher.matches()) {
                    String optKeyWithPrefix = matcher.group(1);
                    boolean requiresArgument = !matcher.group(2).isEmpty();
                    opt(optKeyWithPrefix, requiresArgument);
                    continue;
                }
            }
            {
                Matcher matcher = longOptRegexPattern.matcher(s);
                if (matcher.matches()) {
                    String optKeyWithPrefix = matcher.group(1);
                    boolean requiresArgument = !matcher.group(3).isEmpty();
                    opt(optKeyWithPrefix, requiresArgument);
                    continue;
                }
            }
            throw new IllegalArgumentException(CurlyBracketPatternExtension.format("GetOpt: invalid option {}", s));
        }
        return this;
    }

    private GetOpt opt(String optKeyWithPrefix, boolean requiresArgument) {
        if (optKeyWithPrefix == null || optKeyWithPrefix.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (options.containsKey(optKeyWithPrefix)) {
            throw new RuntimeException(CurlyBracketPatternExtension.format("GetOpt: duplicate option {}", optKeyWithPrefix));
        }
        options.put(optKeyWithPrefix, requiresArgument);
        return this;
    }

    public List<Map.Entry<String, String>> parse(String[] args) {
        if (args == null) {
            throw new IllegalArgumentException();
        }

        List<Map.Entry<String, String>> result = new LinkedList<>();

        Iterator<String> it = Arrays.stream(args).iterator();
        boolean meetsDashDash = false;
        while (it.hasNext()) {
            String arg = it.next();
            if (arg == null) {
                throw new IllegalArgumentException();
            }

            if (Objects.equals(arg, "--")) {
                meetsDashDash = true;
                continue;
            }

            if (meetsDashDash) {
                result.add(new AbstractMap.SimpleImmutableEntry<>(NON_OPT_KEY, arg));
                continue;
            }

            if (options.containsKey(arg)) {
                // e.g. -u User --user User
                if (options.get(arg)) {
                    if (it.hasNext()) {
                        result.add(new AbstractMap.SimpleImmutableEntry<>(arg, it.next()));
                    } else {
                        throw new RuntimeException(CurlyBracketPatternExtension.format("GetOpt: option {} requires an argument but there is none", arg));
                    }
                } else {
                    result.add(new AbstractMap.SimpleImmutableEntry<>(arg, null));
                }
                continue;
            }

            if (arg.length() > 2 && arg.charAt(0) == '-' && arg.charAt(1) != '-') {
                // e.g. -uUser -Duser=User
                String a0 = arg.substring(0, 2);
                if (options.containsKey(a0)) {
                    if (options.get(a0)) {
                        String a1 = arg.substring(2);
                        result.add(new AbstractMap.SimpleImmutableEntry<>(a0, a1));
                    } else {
                        throw new RuntimeException(CurlyBracketPatternExtension.format("GetOpt: option {} requires no argument but there is one", a0));
                    }
                    continue;
                }
            }

            if (arg.length() > 2 && arg.charAt(0) == '-' && arg.charAt(1) == '-') {
                int assignmentIndex = arg.indexOf('=');
                if (assignmentIndex >= 0) {
                    // e.g. --user=User
                    String a0 = arg.substring(0, assignmentIndex);
                    if (options.containsKey(a0)) {
                        if (options.get(a0)) {
                            String a1 = arg.substring(assignmentIndex + 1);
                            result.add(new AbstractMap.SimpleImmutableEntry<>(a0, a1));
                        } else {
                            throw new RuntimeException(CurlyBracketPatternExtension.format("GetOpt: option {} requires no argument but there is one", a0));
                        }
                        continue;
                    }
                }
            }

            result.add(new AbstractMap.SimpleImmutableEntry<>(NON_OPT_KEY, arg));
        }

        return result;
    }
}
