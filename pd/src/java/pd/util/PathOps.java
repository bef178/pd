package pd.util;

import java.util.Arrays;
import java.util.LinkedList;

import lombok.NonNull;

/**
 * POSIX file path string manipulation.
 * Not accept empty string. To the kernel, empty string is never `.`.
 * <a href="https://tools.ietf.org/rfc/rfc3986.txt">rfc3986</a>
 */
public class PathOps {

    public static final PathOps singleton = new PathOps();

    public String basename(String path) {
        return basename(path, null);
    }

    /**
     * strip directory and suffix from path; trailing `/`(s) will be ignored
     */
    public String basename(@NonNull String path, String suffix) {
        throwIfEmpty(path);

        // trim trailing '/'(s)
        int endIndex = path.length();
        while (endIndex - 1 >= 0 && path.charAt(endIndex - 1) == '/') {
            endIndex--;
        }

        // and check for "/"
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
     * strip the last segment from path; trailing `/`(s) will be ignored
     * won't recognize `.` or `..`
     */
    public String dirname(@NonNull String path) {
        throwIfEmpty(path);

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
     * within the last segment, skip leading `.`(s) and trailing `/`(s), get string starting at the next `.`
     * extname cannot be basename
     */
    public String extname(@NonNull String path) {
        throwIfEmpty(path);

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

    public boolean isAbsolutePath(@NonNull String path) {
        throwIfEmpty(path);
        return path.charAt(0) == '/';
    }

    public String join(@NonNull String path, @NonNull String... more) {
        return join(path, Arrays.asList(more));
    }

    /**
     * join inputs with `/`, no trim
     */
    public String join(@NonNull String path, @NonNull Iterable<String> more) {
        throwIfEmpty(path);
        StringBuilder sb = new StringBuilder().append(path);
        for (String another : more) {
            sb.append('/').append(another);
        }
        return sb.toString();
    }

    /**
     * normalize("a/./..//") => "."
     */
    public String normalize(@NonNull String path) {
        throwIfEmpty(path);

        if (path.equals("/")) {
            return "/";
        }
        boolean absolute = isAbsolutePath(path);
        String[] segments = normalize(path.split("/"));
        // a collapsed result is either the root (absolute) or the current directory (relative);
        // String.join renders both as "", so decide by the original path's absoluteness
        if (segments.length == 0 || (segments.length == 1 && segments[0].isEmpty())) {
            return absolute ? "/" : ".";
        }
        return String.join("/", segments);
    }

    private String[] normalize(@NonNull String[] a) {
        // an empty array (from splitting "/") denotes the absolute root with no segments
        if (a.length == 0) {
            return a;
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
                            // ".." goes beyond the absolute root
                            throw new IllegalArgumentException("path goes beyond root");
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
     * relativize("/a", "/b") => "../b"
     */
    public String relativize(@NonNull String from, @NonNull String to) {
        throwIfEmpty(from, "from");
        throwIfEmpty(to, "to");

        if (isAbsolutePath(from) != isAbsolutePath(to)) {
            throw new IllegalArgumentException();
        }
        String relative = String.join("/", relativize(from.split("/", -1), to.split("/", -1)));
        // from == to collapses to nothing; never return "" (the current directory)
        return relative.isEmpty() ? "." : relative;
    }

    private String[] relativize(String[] from, String[] to) {
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
     * resolve("/a/b", "c") => "/a/b/c"
     */
    public String resolve(@NonNull String path, @NonNull String... more) {
        throwIfEmpty(path);

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

    public int compare(@NonNull String path, @NonNull String another) {
        throwIfEmpty(path);
        throwIfEmpty(another, "another");

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

    private void throwIfEmpty(String path) {
        throwIfEmpty(path, "path");
    }

    private void throwIfEmpty(String path, String argLiteral) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException(String.format("`%s` should not be empty", argLiteral));
        }
    }
}
