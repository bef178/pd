package pd.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.SneakyThrows;

import static pd.util.PathOps.throwIfEmpty;

public class FileOps {

    public static final FileOps singleton = new FileOps();

    /**
     * Starting from `pathPrefix`, list the next nodes in the file tree. `pathPrefix` need not be an existing file or directory.
     * `pathPrefix` might be empty.
     * Results are sorted.
     * Directories end with `/`.
     * - "d" => ["d/"]
     * - "d/" => ["d/d/", "d/f"]
     * - "f" => ["f"]
     * - "lo" => ["lo/", "lower/", "long"]
     */
    public List<String> list(@NonNull String pathPrefix) {
        String d;
        if (pathPrefix.equals(".")) {
            d = "";
        } else if (pathPrefix.equals("./")) {
            d = "";
        } else if (pathPrefix.equals("..")) {
            d = "../";
        } else if (pathPrefix.endsWith("/")) {
            d = pathPrefix;
        } else {
            int lastIndex = pathPrefix.lastIndexOf('/');
            if (lastIndex >= 0) {
                d = pathPrefix.substring(0, lastIndex + 1);
            } else {
                d = "";
            }
        }
        if (d.isEmpty()) {
            d = ".";
        }
        List<String> a = listDirectory(d, 1, null);
        if (a == null) {
            return Collections.emptyList();
        }
        return a.stream()
                .filter(s1 -> s1.startsWith(pathPrefix))
                .sorted(PathOps.singleton::compare)
                .collect(Collectors.toList());
    }

    /**
     * `pathPrefix` might be empty.
     */
    public List<String> listAll(@NonNull String pathPrefix) {
        return list(pathPrefix).stream()
                .flatMap(s -> {
                    Path p = Paths.get(s);
                    if (Files.isDirectory(p)) {
                        List<Path> a1 = listDirectoryDepthFirstSearch(p, Integer.MAX_VALUE, null);
                        if (a1 == null) {
                            a1 = Collections.emptyList();
                        }
                        return a1.stream().filter(p1 -> !Files.isDirectory(p1));
                    } else {
                        return Stream.of(p);
                    }
                })
                .map(this::pathToString)
                .sorted(PathExtension::compare)
                .collect(Collectors.toList());
    }

    /**
     * List offspring of `directory` down to specified depth.
     * `depth` should be positive.
     * - Leading "./" will be trimmed.
     * - A trailing "/" will be present for directory.
     */
    public List<String> listDirectory(@NonNull String directory, int depth, AtomicBoolean abortRequested) {
        throwIfEmpty(directory, "directory");

        List<Path> paths = listDirectoryDepthFirstSearch(Paths.get(directory), depth, abortRequested);
        if (paths == null) {
            return null;
        }
        return paths.stream().map(this::pathToString).collect(Collectors.toList());
    }

    private List<Path> listDirectoryDepthFirstSearch(@NonNull Path src, final int depth, AtomicBoolean abortRequested) {
        if (!Files.isDirectory(src)) {
            return null;
        }
        if (abortRequested != null && abortRequested.get()) {
            return null;
        }
        LinkedList<Path> results = new LinkedList<>();
        if (depth > 0) {
            List<Path> children;
            try (Stream<Path> stream = Files.list(src)) {
                children = stream.collect(Collectors.toList());
            } catch (IOException e) {
                children = null;
            }
            if (children != null) {
                for (Path child : children) {
                    if (abortRequested != null && abortRequested.get()) {
                        return null;
                    }
                    results.add(child);
                    if (Files.isDirectory(child)) {
                        List<Path> childrenOfChild = listDirectoryDepthFirstSearch(child, depth - 1, abortRequested);
                        if (childrenOfChild != null) {
                            results.addAll(childrenOfChild);
                        }
                    }
                }
            }
        }
        return results;
    }

    @SneakyThrows
    @SuppressWarnings("unused")
    private List<Path> listDirectoryBreadthFirstSearch(@NonNull Path src, int depth, AtomicBoolean abortRequested) {
        if (!Files.isDirectory(src)) {
            return null;
        }

        if (abortRequested != null && abortRequested.get()) {
            return null;
        }

        List<Path> results = new LinkedList<>();
        List<Path> thisQ = new LinkedList<>();
        thisQ.add(src);
        while (depth != 0) {
            if (abortRequested != null && abortRequested.get()) {
                return null;
            }

            List<Path> nextQ = new LinkedList<>();
            while (!thisQ.isEmpty()) {
                Path first = thisQ.remove(0);
                try (Stream<Path> stream = Files.list(first)) {
                    stream.forEach(p -> {
                        results.add(p);
                        if (Files.isDirectory(p)) {
                            nextQ.add(p);
                        }
                    });
                }
            }
            if (nextQ.isEmpty()) {
                break;
            }
            thisQ = nextQ;
            depth--;
        }
        return results;
    }

    private String pathToString(Path p) {
        String s = p.toString();
        if (s.startsWith("./")) {
            s = s.substring(2);
        }
        if (Files.isDirectory(p)) {
            s += "/";
        }
        return s;
    }

    public boolean copyRecursively(@NonNull String src, @NonNull String dst, AtomicBoolean abortRequested) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");

        return copyRecursively(Paths.get(src), Paths.get(dst), abortRequested);
    }

    /**
     * `src` must exist
     * `dst` must not exist but its parent must be directory
     */
    private boolean copyRecursively(@NonNull Path src, @NonNull Path dst, AtomicBoolean abortRequested) {
        if (!Files.exists(src) || Files.exists(dst)) {
            return false;
        }
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (Files.isDirectory(src)) {
            try {
                Files.createDirectory(dst);
            } catch (IOException ignored) {
                return false;
            }
            try (Stream<Path> stream = Files.list(src)) {
                List<Path> children = stream.collect(Collectors.toList());
                for (Path child : children) {
                    if (abortRequested != null && abortRequested.get()) {
                        return false;
                    }
                    Path dstChild = dst.resolve(child.getFileName());
                    if (!copyRecursively(child, dstChild, abortRequested)) {
                        return false;
                    }
                }
            } catch (IOException ignored) {
                return false;
            }
            return true;
        } else {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            try {
                Files.copy(src, dst);
                return true;
            } catch (IOException ignored) {
            }
            return false;
        }
    }

    public boolean removeRecursively(@NonNull String directory, AtomicBoolean abortRequested) {
        throwIfEmpty(directory, "directory");
        return removeRecursively(Paths.get(directory), abortRequested);
    }

    private boolean removeRecursively(@NonNull Path src, AtomicBoolean abortRequested) {
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (Files.isDirectory(src)) {
            List<Path> children = listDirectoryDepthFirstSearch(src, 1, abortRequested);
            if (children != null) {
                for (Path child : children) {
                    if (abortRequested != null && abortRequested.get()) {
                        return false;
                    }
                    if (!removeRecursively(child, abortRequested)) {
                        return false;
                    }
                }
            }
        }
        try {
            if (!Files.deleteIfExists(src)) {
                return false;
            }
        } catch (IOException ignored) {
            return false;
        }
        return true;
    }
}
