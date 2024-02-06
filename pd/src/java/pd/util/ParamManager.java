package pd.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;

public class ParamManager {

    public static ParamManager parse(String commandLineOptionString, String[] commandLineArgs) {
        return parse(commandLineOptionString, commandLineArgs, (String) null, null);
    }

    public static ParamManager parse(String commandLineOptionString, String[] commandLineArgs, Enum<?> commandLineDotConfigKey, String defaultDotConfigPath) {
        return parse(commandLineOptionString, commandLineArgs, commandLineDotConfigKey.name(), defaultDotConfigPath);
    }

    public static ParamManager parse(String commandLineOptString, String[] commandLineArgs, String commandLineDotConfigKey, String defaultDotConfigPath) {
        ParamManager o = new ParamManager();
        o.init(commandLineOptString, commandLineArgs, commandLineDotConfigKey, defaultDotConfigPath);
        return o;
    }

    private final List<Map.Entry<String, String>> commandLineParams = new LinkedList<>();

    private final Map<String, String> dotConfigParams = new LinkedHashMap<>();

    private ParamManager() {
        // dummy
    }

    private void init(String commandLineOptString, String[] commandLineArgs, String commandLineConfigFileKey, String defaultConfigFilePath) {
        commandLineParams.addAll(GetOpt.parse(commandLineOptString, commandLineArgs));
        dotConfigParams.putAll(parseDotConfig(getFromCommandLineOrDefault(commandLineConfigFileKey, defaultConfigFilePath)));
    }

    private Map<String, String> parseDotConfig(String path) {
        if (path == null) {
            return Collections.emptyMap();
        }
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(path))) {
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties.entrySet().stream()
                .collect(Collectors.toMap(
                        a -> (String) a.getKey(),
                        a -> (String) a.getValue(),
                        (prev, next) -> next,
                        LinkedHashMap::new
                ));
    }

    public String get(Enum<?> key) {
        return get(key.name());
    }

    public String get(String key) {
        return getFromCommandLineOrDefault(key, dotConfigParams.getOrDefault(key, getFromSystemEnvironment(key)));
    }

    public List<String> getNonOptionArguments() {
        return getAllFromCommandLine(GetOpt.NON_OPT_KEY);
    }

    private String getFromCommandLine(String key) {
        List<String> values = getAllFromCommandLine(key);
        if (values == null || values.isEmpty()) {
            return null;
        } else {
            return values.get(values.size() - 1);
        }
    }

    private String getFromCommandLineOrDefault(String key, String defaultValue) {
        String value = getFromCommandLine(key);
        if (value != null) {
            return value;
        }
        return defaultValue;
    }

    private List<String> getAllFromCommandLine(String key) {
        if (key == null) {
            return null;
        }
        final String k;
        if (GetOpt.NON_OPT_KEY.equals(key)) {
            k = key;
        } else {
            if (key.length() == 1) {
                k = '-' + key;
            } else if (key.length() > 1) {
                k = "--" + key;
            } else {
                k = key;
            }
        }
        return commandLineParams.stream()
                .filter(a -> Objects.equals(a.getKey(), k))
                .map(Map.Entry::getValue)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    private static String getFromSystemEnvironment(String key) {
        return System.getenv(key);
    }
}
