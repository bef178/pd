package pd.util;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * This class itself is to manipulate path strings. With extensions, it is able to do file existence check, content modification, etc.<br/>
 * <br/>
 * @see <a href="https://tools.ietf.org/rfc/rfc3986.txt">rfc3986</a><br/>
 */
public class PathUtil extends FileSystemUtil {

    /**
     * get the last component of a path; trailing '/'(s) will be ignored<br/>
     */
    public static String basename(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int endIndex = -1;
        for (int i = path.length() - 1; i >= 0; i--) {
            int ch = path.charAt(i);
            if (endIndex == -1) {
                if (ch == '/') {
                    // ignored: trailing '/'
                } else {
                    endIndex = i + 1;
                }
            } else {
                if (ch == '/') {
                    return path.substring(i + 1, endIndex);
                } else {
                    // ignored: found partial, go ahead
                }
            }
        }
        return endIndex == -1 ? "/" : path.substring(0, endIndex);
    }

    /**
     * strip the last component of a path; trailing '/'(s) will be ignored<br/>
     */
    public static String dirname(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int endIndex = -1;
        int candidateIndex = -1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == '/') {
                if (candidateIndex == -1) {
                    candidateIndex = i;
                }
            } else if (candidateIndex != -1) {
                endIndex = candidateIndex;
                candidateIndex = -1;
            }
        }
        if (endIndex == -1) {
            if (candidateIndex == 0) {
                return "/";
            } else {
                return ".";
            }
        } else if (endIndex == 0) {
            return "/";
        }
        return path.substring(0, endIndex);
    }

    public static String fileext(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int i = path.lastIndexOf('.');
        if (i == -1) {
            return null;
        }
        return path.substring(i + 1);
    }

    public static boolean isAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return path.charAt(0) == '/';
    }

    public static String join(String path, String... more) {
        return Paths.get(path, more).toString();
    }

    /**
     * will ignore the possible absolute path in the middle
     */
    @SuppressWarnings("unused")
    private static String join1(String path, String... more) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder().append(path);
        for (String another : more) {
            if (another == null || another.isEmpty()) {
                throw new IllegalArgumentException();
            }
            sb.append('/').append(another);
        }
        return sb.toString();
    }

    /**
     * normalized path will not end with '/'<br/>
     * <br/>
     * return e.g. "/a/b/c" or "./a/b/c" or "../a/b/c"
     */
    public static String normalize(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (path.equals("/")) {
            return "/";
        }
        return String.join("/", normalize(path.split("/")));
    }

    private static String[] normalize(String[] a) {
        assert a != null;

        LinkedList<String> segments = new LinkedList<>();

        int i = 0;
        switch (a[i]) {
            case "":
            case ".":
            case "..":
                segments.add(a[i++]);
                break;
            default:
                segments.add(".");
                break;
        }

        while (i < a.length) {
            switch (a[i]) {
                case "":
                case ".":
                    i++;
                    break;
                case "..":
                    switch (segments.getLast()) {
                        case "":
                            i++;
                            break;
                        case ".":
                            segments.removeLast();
                            segments.add(a[i++]);
                            break;
                        case "..":
                            segments.add(a[i++]);
                            break;
                        default:
                            segments.removeLast();
                            i++;
                            break;
                    }
                    break;
                default:
                    segments.add(a[i++]);
                    break;
            }
        }

        return segments.toArray(new String[0]);
    }

    /**
     * "/a/b/c", "/d" => "../../../d"
     */
    public static String relativize(String from, String to) {
        if (from == null || from.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (to == null || to.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (isAbsolutePath(from) != isAbsolutePath(to)) {
            throw new IllegalArgumentException();
        }
        return String.join("/", relativize(from.split("/"), to.split("/")));
    }

    private static String[] relativize(String[] from, String[] to) {
        assert from != null;
        assert to != null;

        from = normalize(from);
        to = normalize(to);

        int start = 0;
        while (start < to.length && start < from.length) {
            if (!to[start].equals(from[start])) {
                break;
            }
            ++start;
        }

        String[] a = new String[from.length - start + to.length - start];
        Arrays.fill(a, 0, from.length - start, "..");
        System.arraycopy(to, start, a, from.length - start, to.length - start);

        return a;
    }

    public static String resolve(String path, String... more) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }
        StringBuilder sb = new StringBuilder().append(path);
        for (String another : more) {
            if (another == null || another.isEmpty()) {
                throw new IllegalArgumentException();
            }
            if (isAbsolutePath(another)) {
                sb.setLength(0);
                sb.append(another);
            } else {
                sb.append('/').append(another);
            }
        }
        return sb.toString();
    }
}
