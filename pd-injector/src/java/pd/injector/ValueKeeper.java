package pd.injector;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import pd.util.EntriesCodec;

@Slf4j
public class ValueKeeper {

    private final Map<String, Object> cache = Collections.synchronizedMap(new LinkedHashMap<>());

    public Object get(String key) {
        return cache.get(key);
    }

    public Object put(String key, Object value) {
        return cache.put(key, value);
    }

    public void putAll(Map<String, Object> values) {
        cache.putAll(values);
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public void clear() {
        cache.clear();
    }

    public void loadProperties(String s) {
        EntriesCodec codec = new EntriesCodec('=', '\n');
        List<Map.Entry<String, String>> m = codec.decode(s);
        for (Map.Entry<String, String> entry : m) {
            if (entry.getKey().startsWith("#") || entry.getKey().isEmpty() || entry.getValue() == null) {
                continue;
            }
            cache.put(entry.getKey().trim(), entry.getValue().trim());
        }
    }

    public void loadYaml(String s) {
        Map<String, Object> m = new Yaml().load(s);
        cache.putAll(flatten(m, null));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> flatten(Map<String, Object> src, String keyPrefix) {
        LinkedHashMap<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : src.entrySet()) {
            String key = keyPrefix == null ? entry.getKey() : (keyPrefix + "." + entry.getKey());
            if (entry.getValue() instanceof Map) {
                result.putAll(flatten((Map<String, Object>) entry.getValue(), key));
            } else {
                result.put(key, entry.getValue());
            }
        }
        return result;
    }
}
