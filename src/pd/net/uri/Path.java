package pd.net.uri;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * path is a string<br/>
 * https://tools.ietf.org/rfc/rfc3986.txt<br/>
 */
public final class Path {

    /**
     * get last segment
     */
    public static String getBasename(String path) {
        String[] a = path2segs(path);
        return a[a.length - 1];
    }

    public static String getParent(String path) {
        String[] a = path2segs(path);
        a = Arrays.copyOfRange(a, 0, a.length - 1);
        return segs2path(a);
    }

    public static boolean isAbsolute(String path) {
        return isAbsolute(path2segs(path));
    }

    private static boolean isAbsolute(String[] segs) {
        return segs[0].equals("");
    }

    /**
     * return e.g. "/a/b/c" or "./a/b/c" or "../a/b/c"
     */
    public static String normalize(String path) {
        String[] a = path2segs(path);
        LinkedList<String> segs = new LinkedList<String>();
        for (String seg : a) {
            segs.add(seg);
        }

        switch (segs.get(0)) {
            case "":
                break;
            case ".":
                break;
            case "..":
                break;
            default:
                segs.add(0, ".");
                break;
        }

        int i = 1;
        while (i < segs.size()) {
            switch (segs.get(i)) {
                case "":
                    // align to bash cd
                    segs.remove(i);
                    continue;
                case ".":
                    segs.remove(i);
                    continue;
                case "..":
                    switch (segs.get(i - 1)) {
                        case "":
                            return null;
                        case ".":
                            segs.remove(i - 1);
                            continue;
                        case "..":
                            // dummy
                            break;
                        default:
                            --i;
                            segs.remove(i);
                            segs.remove(i);
                            continue;
                    }
                    break;
                default:
                    break;
            }
            ++i;
        }

        return segs2path(segs);
    }

    private static String[] path2segs(String path) {
        return path.split("/");
    }

    public static String relativize(String base, String resolved) {
        if (!isAbsolute(base) && isAbsolute(resolved)) {
            return null;
        }

        String[] dst = path2segs(resolved);
        String[] src = path2segs(base);

        int start = 0;
        while (start < dst.length && start < src.length) {
            if (!dst[start].equals(src[start])) {
                break;
            }
            ++start;
        }

        String[] a = new String[src.length - start + dst.length - start];
        Arrays.fill(a, 0, src.length - start, "..");
        System.arraycopy(dst, start, a, src.length - start, dst.length - start);

        return segs2path(a);
    }

    public static String resolve(String base, String relative) {
        if (isAbsolute(relative)) {
            return null;
        }
        return segs2path(base, relative);
    }

    public static String resolveParent(String base, String relative) {
        return resolve(getParent(base), relative);
    }

    private static String segs2path(List<String> segs) {
        return String.join("/", segs);
    }

    private static String segs2path(String... segs) {
        return String.join("/", segs);
    }

    private Path() {
        // private dummy
    }
}
