package pd.injector.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Util {

    /**
     * return null if fail to find such resource
     */
    public static InputStreamReader resourceAsReader(String name) {
        InputStream inputStream = resourceAsStream(name);
        if (inputStream == null) {
            return null;
        }
        return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    public static InputStream resourceAsStream(String name) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
    }

    private Util() {
        // private dummy
    }
}
