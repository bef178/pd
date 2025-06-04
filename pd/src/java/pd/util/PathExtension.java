package pd.util;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * manipulate string that represent path<br/>
 * @see <a href="https://tools.ietf.org/rfc/rfc3986.txt">rfc3986</a><br/>
 */
public class PathExtension {

    public static String basename(String path) {
        return basename(path, null);
    }

    /**
     * strip directory and suffix from path; trailing '/'(s) will be ignored<br/>
     * see `man 3 basename`<br/>
     */
    public static String basename(String path, String suffix) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int startIndex = 0;
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
                    startIndex = i + 1;
                    break;
                } else {
                    // ignored: found partial, go ahead
                }
            }
        }

        if (endIndex == -1) {
            return "/";
        }

        if (suffix != null && !suffix.isEmpty()) {
            int i = path.lastIndexOf(suffix, endIndex);
            if (i > startIndex) {
                endIndex = i;
            }
        }
        return path.substring(startIndex, endIndex);
    }

    /**
     * strip last component from path; trailing '/'(s) will be ignored<br/>
     * see `man 3 dirname`<br/>
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

    /**
     * get the file extension; the dot will be included<br/>
     * see node.js path.extname<br/>
     */
    public static String extname(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int i = path.lastIndexOf('.');
        switch (path.lastIndexOf('.')) {
            case -1:
            case 0:
                return "";
            default:
                return path.substring(i);
        }
    }

    public static boolean isAbsolutePath(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return path.charAt(0) == '/';
    }

    /**
     * will ignore the possible absolute path in the middle
     */
    public static String join(String path, Iterable<String> more) {
        // not use Paths. it will trim "//" to "/"
        // return Paths.get(path, more).toString();
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if (more == null) {
            throw new IllegalArgumentException();
        }
        for (String another : more) {
            if (another == null || another.isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
        StringBuilder sb = new StringBuilder().append(path);
        for (String another : more) {
            sb.append('/').append(another);
        }
        return sb.toString();
    }

    public static String join(String path, String... more) {
        return join(path, Arrays.asList(more));
    }

    /**
     * normalize() removes unnecessary '.' and '..' and trailing '/'<br/>
     * normalize("./../abc/") => "../abc"<br/>
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
     * u.relativize(v) => relativize(u, v) => v - u<br/>
     * relativize("/a/b", "/a/b/c") => "c"<br/>
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

    /**
     * u.resolve(v) => resolve(u, v) => v + u<br/>
     * resolve("/a/b", "c") => "/a/b/c"<br/>
     * <br/>
     * if both u and v are relative:<br/>
     * - u.relativize(u.resolve(v)).equals(v)<br/>
     * - u.resolve(u.relativize(v)).equals(v)<br/>
     */
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

    public static int compare(String path, String another) {
        if (path == null || another == null) {
            throw new IllegalArgumentException();
        }

        // directory first
        if (path.endsWith("/")) {
            if (!another.endsWith("/")) {
                return -1;
            }
        } else {
            if (another.endsWith("/")) {
                return 1;
            }
        }

        int[] endIndexes;
        {
            endIndexes = new int[] { path.length(), another.length() };
            int i = endIndexes[0] - 1;
            int j = endIndexes[1] - 1;
            while (i >= 0 && j >= 0) {
                if (path.charAt(i) != another.charAt(j)) {
                    break;
                }
                if (path.charAt(i) == '.') {
                    endIndexes[0] = i;
                    endIndexes[1] = j;
                }
                i--;
                j--;
            }
        }

        int i = 0;
        while (i < endIndexes[0] && i < endIndexes[1]) {
            int ch = path.charAt(i);
            int ch2 = another.charAt(i);
            if (ch != ch2) {
                if (ch == '/') {
                    return -1;
                } else if (ch2 == '/') {
                    return 1;
                } else {
                    return ch - ch2;
                }
            }
            i++;
        }
        if (endIndexes[0] == endIndexes[1]) {
            return 0;
        } else if (i == endIndexes[0]) {
            return -1;
        } else {
            return 1;
        }
    }
}
