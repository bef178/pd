package pd.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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
        try (InputStreamReader reader = resourceAsReader(resourceName)) {
            properties.load(reader);
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

    public static String resourceAsString(String resourceName) {
        return new String(resourceAsBytes(resourceName), StandardCharsets.UTF_8);
    }

    public static InputStream resourceAsStream(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        return classLoader != null
                ? classLoader.getResourceAsStream(resourceName)
                : ClassLoader.getSystemResourceAsStream(resourceName);
    }

    public static InputStreamReader resourceAsReader(String name) {
        InputStream inputStream = resourceAsStream(name);
        if (inputStream == null) {
            return null;
        }
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    private ResourceExtension() {
        // private dummy
    }
}
