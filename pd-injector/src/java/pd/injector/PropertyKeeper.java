package pd.injector;

import java.io.InputStreamReader;
import java.util.Properties;
import static pd.injector.util.Util.resourceAsReader;

class PropertyKeeper {

    private final Properties cache = new Properties();

    public void load(String name) {
        try (InputStreamReader reader = resourceAsReader(name)) {
            cache.load(reader);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load resource file \"" + name + "\"", e);
        }
    }

    public boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public String getProperty(String key) {
        return cache.getProperty(key);
    }

    public void clear() {
        cache.clear();
    }
}
