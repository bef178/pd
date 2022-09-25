package pd.util;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * path is a string<br/>
 * https://tools.ietf.org/rfc/rfc3986.txt<br/>
 */
public class PathExtension {

    /**
     * get the last component of a path; trailing '/'(s) will be ignored<br/>
     */
    public static String getBasename(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        int endIndex = -1;
        for (int i = path.length() - 1; i >= 0; i--) {
            int ch = path.charAt(i);
            if (endIndex == -1) {
                if (ch == '/') {
                    // ignore trailing '/'
                    continue;
                } else {
                    endIndex = i + 1;
                }
            } else {
                if (ch == '/') {
                    return path.substring(i + 1, endIndex);
                } else {
                    // found partial, go ahead
                    continue;
                }
            }
        }
        return endIndex == -1 ? "/" : path.substring(0, endIndex);
    }

    /**
     * strip the last component of a path; trailing '/'(s) will be ignored<br/>
     */
    public static String getParent(String path) {
        if (path.isEmpty()) {
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

    public static boolean isAbsolute(String path) {
        assert path != null;
        return path.length() > 0 && path.charAt(0) == '/';
    }

    /**
     * return e.g. "/a/b/c" or "./a/b/c" or "../a/b/c"
     */
    public static String normalize(String path) {
        if (path.isEmpty()) {
            throw new IllegalArgumentException();
        }

        if (path.equals("/")) {
            return "/";
        }
        return String.join("/", normalize(path.split("/")));
    }

    private static String[] normalize(String[] a) {
        assert a != null;

        LinkedList<String> segs = new LinkedList<String>();

        int i = 0;
        switch (a[i]) {
            case "":
            case ".":
            case "..":
                segs.add(a[i++]);
                break;
            default:
                segs.add(".");
                break;
        }

        while (i < a.length) {
            switch (a[i]) {
                case "":
                    i++;
                    break;
                case ".":
                    i++;
                    break;
                case "..":
                    switch (segs.getLast()) {
                        case "":
                            i++;
                            break;
                        case ".":
                            segs.removeLast();
                            segs.add(a[i++]);
                            break;
                        case "..":
                            segs.add(a[i++]);
                            break;
                        default:
                            segs.removeLast();
                            i++;
                            break;
                    }
                    break;
                default:
                    segs.add(a[i++]);
                    break;
            }
        }

        return segs.toArray(new String[segs.size()]);
    }

    /**
     * "/a/b/c", "/d" => "../../../d"
     */
    public static String relativize(String from, String to) {
        assert from != null;
        assert to != null;
        assert isAbsolute(from) == isAbsolute(to);
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
     * "a", "b" => "a/b"
     */
    public static String resolve(String path, String another) {
        if (isAbsolute(another)) {
            return another;
        } else {
            return path + "/" + another;
        }
    }
}
