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

        // trim trailing '/'(s)
        int endIndex = path.length();
        while (endIndex - 1 >= 0 && path.charAt(endIndex - 1) == '/') {
            endIndex--;
        }

        // special exec against "////"
        if (endIndex == 0) {
            return "/";
        }

        int startIndex = path.lastIndexOf('/', endIndex - 1) + 1;

        if (suffix != null && !suffix.isEmpty()) {
            int i = path.lastIndexOf(suffix, endIndex);
            if (i > startIndex) {
                endIndex = i;
            }
        }

        return path.substring(startIndex, endIndex);
    }

    /**
     * strip the last segment from path; trailing '/'(s) will be ignored<br/>
     * won't recognize `.` or `..`<br/>
     */
    public static String dirname(@NonNull String path) {
        if (path.isEmpty()) {
            return "";
        }

        int endIndex = path.length();
        while (endIndex - 1 >= 0 && path.charAt(endIndex - 1) == '/') {
            endIndex--;
        }

        if (endIndex == 0) {
            return "/";
        }

        endIndex = path.lastIndexOf('/', endIndex - 1);
        if (endIndex < 0) {
            return "";
        }

        while (endIndex - 1 >= 0 && path.charAt(endIndex - 1) == '/') {
            endIndex--;
        }
        if (endIndex == 0) {
            return "/";
        }

        return path.substring(0, endIndex);
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

        int endIndex = path.length();
        while (endIndex - 1 >= 0 && path.charAt(endIndex - 1) == '/') {
            endIndex--;
        }

        if (endIndex == 0) {
            return "";
        }

        int startIndex = path.lastIndexOf('/', endIndex - 1) + 1;
        while (startIndex < endIndex && path.charAt(startIndex) == '.') {
            startIndex++;
        }

        while (++startIndex < endIndex) {
            if (path.charAt(startIndex) == '.') {
                return path.substring(startIndex, endIndex);
            }
        }
        return "";
    }

    public static boolean isAbsolutePath(@NonNull String path) {
        if (path.isEmpty()) {
            return false;
        }
        return path.charAt(0) == '/';
    }

    /**
     * connect given inputs with '/'
     */
    public static String join(@NonNull String path, @NonNull Iterable<String> more) {
        // not use Paths. it will trim "//" to "/"
        // return Paths.get(path, more).toString();
        StringBuilder sb = new StringBuilder().append(path);
        for (String another : more) {
            sb.append('/').append(another);
        }
        return sb.toString();
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

        if (path.equals("/")) {
            return "/";
        }
        return String.join("/", normalize(path.split("/")));
    }

    private static String[] normalize(@NonNull String[] a) {
        if (a.length == 0) {
            throw new IllegalArgumentException();
        }

        LinkedList<String> segments = new LinkedList<>();

        int i = 0;
        while (i < a.length && a[i].equals(".")) {
            i++;
        }
        if (i < a.length) {
            segments.add(a[i++]);
        }
        while (i < a.length) {
            switch (a[i]) {
                case "":
                case ".":
                    i++;
                    break;
                case "..":
                    if (segments.isEmpty()) {
                        segments.add(a[i++]);
                        break;
                    }
                    switch (segments.getLast()) {
                        case "":
                            i++;
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
     * u.relativize(v) => relativize(u, v) => v - u<br/>
     * relativize("/a/b", "/a/b/c") => "c"<br/>
     */
    public static String relativize(@NonNull String from, @NonNull String to) {
        if (from.isEmpty()) {
            return to;
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

    /**
     * u.resolve(v) => resolve(u, v) => v + u<br/>
     * resolve("/a/b", "c") => "/a/b/c"<br/>
     * <br/>
     * if u and v are both relative:<br/>
     * - u.relativize(u.resolve(v)) == v<br/>
     * - u.resolve(u.relativize(v)) == v<br/>
     */
    public static String resolve(@NonNull String path, @NonNull String... more) {
        StringBuilder sb = new StringBuilder().append(path);
        for (String another : more) {
            if (another.isEmpty()) {
                continue;
            }
            if (isAbsolutePath(another)) {
                sb.setLength(0);
                sb.append(another);
            } else if (sb.length() > 0) {
                sb.append('/').append(another);
            } else {
                sb.append(another);
            }
        }
        return normalize(sb.toString());
    }

    public static int compare(@NonNull String path, @NonNull String another) {
        int[] a = path.codePoints().toArray();
        int[] b = another.codePoints().toArray();
        for (int i = 0; i < a.length && i < b.length; i++) {
            if (a[i] != b[i]) {
                // directory first
                if (a[i] == '/') {
                    return -1;
                } else if (b[i] == '/') {
                    return 1;
                }
                // given name first
                if (a[i] == '.') {
                    return -1;
                } else if (b[i] == '.') {
                    return 1;
                }
                return Integer.compare(a[i], b[i]);
            }
        }
        if (a.length > b.length) {
            for (int i = b.length; i < a.length; i++) {
                if (a[i] == '/') {
                    return -1;
                }
            }
        } else if (a.length < b.length) {
            for (int i = a.length; i < b.length; i++) {
                if (b[i] == '/') {
                    return 1;
                }
            }
        }
        return Integer.compare(a.length, b.length);
    }
}
