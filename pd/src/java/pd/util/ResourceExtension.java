package pd.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static pd.util.InputStreamExtension.readAllBytes;

/**
 * `string` as resource name(key)
 */
public class ResourceExtension {

    public static byte[] resourceAsBytes(String resourceName) {
        assert resourceName != null;
        try (InputStream stream = resourceAsStream(resourceName)) {
            return readAllBytes(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties resourceAsProperties(String resourceName) throws IOException {
        assert resourceName != null;
        Properties properties = new Properties();
        try (InputStream stream = resourceAsStream(resourceName)) {
            properties.load(stream);
            return properties;
        }
    }

    public static Properties resourceAsPropertiesNoThrow(String resourceName) {
        try {
            return resourceAsProperties(resourceName);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static InputStream resourceAsStream(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader != null
                ? classLoader.getResourceAsStream(resourceName)
                : ClassLoader.getSystemResourceAsStream(resourceName);
    }

    public static String resourceAsString(String resourceName) {
        return new String(resourceAsBytes(resourceName));
    }

    private ResourceExtension() {
        // private dummy
    }
}
