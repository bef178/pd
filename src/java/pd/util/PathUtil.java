package pd.util;

import pd.file.FileAccessor;
import pd.file.PathAccessor;

/**
 * This class itself is to manipulate path strings. With extensions, it is able to do file existence check, content modification, etc.<br/>
 * <br/>
 * @see <a href="https://tools.ietf.org/rfc/rfc3986.txt">rfc3986</a><br/>
 */
@Deprecated
public class PathUtil {

    private static final FileAccessor fileAccessor = FileAccessor.singleton();
    private static final PathAccessor pathAccessor = PathAccessor.singleton();

    /**
     * get the last component of a path; trailing '/'(s) will be ignored<br/>
     */
    public static String basename(String path) {
        return pathAccessor.basename(path);
    }

    /**
     * strip the last component of a path; trailing '/'(s) will be ignored<br/>
     */
    public static String dirname(String path) {
        return pathAccessor.dirname(path);
    }

    public static String fileExtension(String path) {
        return pathAccessor.extname(path);
    }

    public static boolean isAbsolutePath(String path) {
        return pathAccessor.isAbsolutePath(path);
    }

    /**
     * will ignore the possible absolute path in the middle
     */
    public static String join(String path, String... more) {
        return pathAccessor.join(path, more);
    }

    /**
     * normalized path will not end with '/'<br/>
     * <br/>
     * return e.g. "/a/b/c" or "./a/b/c" or "../a/b/c"
     */
    public static String normalize(String path) {
        return pathAccessor.normalize(path);
    }

    /**
     * "/a/b/c", "/d" => "../../../d"
     */
    public static String relativize(String from, String to) {
        return pathAccessor.relativize(from, to);
    }

    public static String resolve(String path, String... more) {
        return pathAccessor.resolve(path, more);
    }

    public static boolean exists(String path) {
        return fileAccessor.exists(path);
    }

    public static boolean mkdir(String path, boolean p) {
        return fileAccessor.makeDirectory(path, p);
    }

    public static byte[] load(String path) {
        return fileAccessor.load(path);
    }

    public static String loadText(String path) {
        return fileAccessor.loadText(path);
    }

    public static void save(String path, byte[] bytes) {
        fileAccessor.save(path, bytes);
    }

    public static void save(String path, String content) {
        fileAccessor.saveText(path, content);
    }
}
