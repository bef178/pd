package pd.util;

import java.util.Arrays;
import java.util.LinkedList;

import lombok.NonNull;

/**
 * Path string manipulation.<br/>
 * <br/>
 * Accepts/produces empty strings (interpreted as the current directory).<br/>
 * Won't create `.` or `..`.<br/>
 * <br/>
 *
 * @see <a href="https://tools.ietf.org/rfc/rfc3986.txt">rfc3986</a><br/>
 */
public class PathExtension {

    private static final PathOps pathOps = PathOps.singleton;

    public static String basename(String path) {
        return basename(path, null);
    }

    /**
     * strip directory and suffix from path; trailing '/'(s) will be ignored
     */
    public static String basename(@NonNull String path, String suffix) {
        if (path.isEmpty()) {
            return "";
        }
        return pathOps.basename(path, suffix);
    }

    /**
     * strip the last segment from path; trailing '/'(s) will be ignored<br/>
     * won't recognize `.` or `..`<br/>
     */
    public static String dirname(@NonNull String path) {
        if (path.isEmpty()) {
            return "";
        }
        return pathOps.dirname(path);
    }

    /**
     * within the last segment, skip leading `.`(s) and trailing '/'(s), get string starting at the next `.`<br/>
     * <br/>
     * extname cannot be basename<br/>
     */
    public static String extname(@NonNull String path) {
        if (path.isEmpty()) {
            return "";
        }
        return pathOps.extname(path);
    }

    public static boolean isAbsolutePath(@NonNull String path) {
        if (path.isEmpty()) {
            return false;
        }
        return pathOps.isAbsolutePath(path);
    }

    /**
     * connect given inputs with '/'
     * no trim
     */
    public static String join(@NonNull String path, @NonNull Iterable<String> more) {
        return pathOps.join(path, more);
    }

    public static String join(@NonNull String path, @NonNull String... more) {
        return join(path, Arrays.asList(more));
    }

    /**
     * normalize() removes unnecessary '.' and '..' and trailing '/'<br/>
     * normalize("./../abc/") => "../abc"<br/>
     */
    public static String normalize(@NonNull String path) {
        if (path.isEmpty()) {
            return "";
        }
        return pathOps.normalize(path);
    }

    public static String relativize(@NonNull String from, @NonNull String to) {
        if (from.isEmpty()) {
            return to;
        }
        return pathOps.relativize(from, to);
    }

    /**
     * u.resolve(v) => resolve(u, v) => v + u<br/>
     * resolve("/a/b", "c") => "/a/b/c"<br/>
     * <br/>
     * if u and v are both relative:<br/>
     * - u.relativize(u.resolve(v)) == v<br/>
     * - u.resolve(u.relativize(v)) == v<br/>
     */
    public static String resolve(@NonNull String path, @NonNull String... more) {
        return pathOps.resolve(path, more);
    }

    public static int compare(@NonNull String path, @NonNull String another) {
        return pathOps.compare(path, another);
    }
}
