package pd.util;

import java.net.URI;

public class UrlExtension {

    public static String resolveUrl(String baseUrl, String relativePath) {
        return URI.create(baseUrl).resolve(relativePath).toString();
    }
}
