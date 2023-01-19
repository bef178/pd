package pd.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static pd.util.InputStreamExtension.readAllBytes;

public class ResourceUtil {

    public static byte[] resourceAsBytes(String resourceName) {
        assert resourceName != null;
        try (InputStream stream = resourceAsStream(resourceName)) {
            return readAllBytes(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Properties resourceAsProperties(String resourceName) {
        assert resourceName != null;
        Properties properties = new Properties();
        try (InputStream stream = resourceAsStream(resourceName)) {
            properties.load(stream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public static InputStream resourceAsStream(String resourceName) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName);
    }

    public static String resourceAsString(String resourceName) {
        return new String(resourceAsBytes(resourceName));
    }

    private ResourceUtil() {
        // private dummy
    }
}
