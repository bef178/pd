package pd.adt;

import java.util.Arrays;
import java.util.LinkedList;

public class Path {

    public static Path wrap(String... a) {
        return new Path(a);
    }

    private String[] a;

    public Path(String path) {
        this(path.split("/"));
    }

    private Path(String[] a) {
        this.a = a;
    }

    public String basename() {
        return a[a.length - 1];
    }

    public boolean isAbsolute() {
        return a[0].equals("");
    }

    public boolean isRelative() {
        return !isAbsolute();
    }

    /**
     * return e.g. "/a/b/c" or "./a/b/c" or "../a/b/c"
     */
    public Path normalize() {
        LinkedList<String> segments = new LinkedList<String>(Arrays.asList(a));

        switch (segments.get(0)) {
            case "":
                break;
            case ".":
                break;
            case "..":
                break;
            default:
                segments.add(0, ".");
                break;
        }

        for (int i = 1; i < segments.size(); ) {
            switch (segments.get(i)) {
                case "":
                    segments.remove(i);
                    continue;
                case ".":
                    segments.remove(i);
                    continue;
                case "..":
                    switch (segments.get(i - 1)) {
                        case "":
                            throw new IllegalArgumentException();
                        case ".":
                            segments.remove(i - 1);
                            continue;
                        case "..":
                            // dummy
                            break;
                        default:
                            --i;
                            segments.remove(i);
                            segments.remove(i);
                            continue;
                    }
                    break;
                default:
                    break;
            }
            ++i;
        }

        return Path.wrap(segments.toArray(new String[0]));
    }

    public Path relativizeFrom(Path from) {
        return from.relativizeTo(this);
    }

    /**
     * suppose these two are at the same start point
     */
    public Path relativizeTo(Path to) {
        String[] dst = to.a;
        String[] src = this.a;

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

        return Path.wrap(a);
    }

    public Path resolve(Path... relatives) {
        int n = 0;
        for (Path relative : relatives) {
            if (relative.isAbsolute()) {
                throw new IllegalArgumentException();
            }
            n += relative.a.length;
        }

        String[] a = Arrays.copyOf(this.a, this.a.length + n);

        int i = this.a.length;
        for (Path relative : relatives) {
            System.arraycopy(relative.a, 0, a, i, relative.a.length);
            i += relative.a.length;
        }
        return Path.wrap(a);
    }

    @Override
    public String toString() {
        return String.join("/", a);
    };
}
