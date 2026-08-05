package pd.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.SneakyThrows;

import static pd.util.PathOps.throwIfEmpty;

/**
 * 目录树中只有两种结点：目录、文件。目录可以是叶结点，文件只能是叶结点。
 * 文件再分为常规文件和特殊文件。其中，符号链接影响了目录树的结构。
 * 基本操作集：枚举目录子结点(读目录)，创建/删除叶结点(写目录)，读/写文件，读/写结点属性。
 * 没有"原地编辑"符号链接内容的系统调用。因此，读写符号链接内容即为读写目标结点内容；读/写符号链接属性则与目标结点无关。
 * 日常操作无需区分符号链接。
 */
class FileOpsCore {

    public List<String> listDirectory(@NonNull String pathToDirectory) {
        throwIfEmpty(pathToDirectory, "pathToDirectory");

        String directory = pathToDirectory.equals(".") ? "" : pathToDirectory;
        // no explicit isDirectory check: NoSuchFileException / NotDirectoryException will be caught
        try (Stream<Path> stream = Files.list(Paths.get(directory))) {
            return stream
                    .map(this::pathToString)
                    .sorted(PathOps.singleton::compare)
                    .collect(Collectors.toList());
        } catch (IOException ignored) {
            return null;
        }
    }

    protected String pathToString(Path p) {
        String s = p.toString();
        if (s.startsWith("./")) {
            s = s.substring(2);
        }
        if (Files.isDirectory(p)) {
            s += "/";
        }
        return s;
    }

    public boolean createDirectory(@NonNull String pathToDirectory, boolean parents) {
        throwIfEmpty(pathToDirectory, "pathToDirectory");

        try {
            Path d = Paths.get(pathToDirectory);
            if (parents) {
                Files.createDirectories(d);
            } else {
                Files.createDirectory(d);
            }
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Remove the directory at `pathToDirectory`.
     * Accept a directory or a symlink to directory.
     * Not follow symlink.
     */
    public boolean removeDirectory(@NonNull String pathToDirectory, boolean recursive, boolean parents, AtomicBoolean abortRequested) {
        throwIfEmpty(pathToDirectory, "pathToDirectory");

        Path d = Paths.get(pathToDirectory);
        if (!Files.isDirectory(d)) {
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (!removeDirectory(d, recursive, abortRequested)) {
            return false;
        }
        if (parents) {
            Path p = d.getParent();
            while (p != null) {
                if (abortRequested != null && abortRequested.get()) {
                    return false;
                }
                // only remove empty ancestor directories; stop at the first non-empty one
                if (!removeDirectory(p, false, abortRequested)) {
                    break;
                }
                p = p.getParent();
            }
        }
        return true;
    }

    private boolean removeDirectory(Path src, boolean recursive, AtomicBoolean abortRequested) {
        if (Files.isSymbolicLink(src)) {
            try {
                return Files.deleteIfExists(src);
            } catch (IOException ignored) {
                return false;
            }
        }
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (recursive) {
            List<String> children = listDirectory(src.toString());
            if (children == null) {
                return false;
            }
            for (String child : children) {
                if (abortRequested != null && abortRequested.get()) {
                    return false;
                }
                Path childPath = Paths.get(child);
                // a symlink is removed as the link itself, never followed to its target;
                // a real directory is recursed into; everything else is removed as a file
                if (Files.isSymbolicLink(childPath)) {
                    if (!removeFile(child)) {
                        return false;
                    }
                } else if (Files.isDirectory(childPath)) {
                    if (!removeDirectory(childPath, true, abortRequested)) {
                        return false;
                    }
                } else {
                    if (!removeFile(child)) {
                        return false;
                    }
                }
            }
        }
        try {
            return Files.deleteIfExists(src);
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Remove the file at `pathToFile`.
     * Accept a file(non-directory).
     * Not follow symlink.
     */
    public boolean removeFile(@NonNull String pathToFile) {
        throwIfEmpty(pathToFile, "pathToFile");

        Path p = Paths.get(pathToFile);
        if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }

        try {
            return Files.deleteIfExists(p);
        } catch (IOException ignored) {
            return false;
        }
    }

    public byte[] load(@NonNull String pathToFile) {
        throwIfEmpty(pathToFile, "pathToFile");

        Path p = Paths.get(pathToFile);
        if (!Files.exists(p) || Files.isDirectory(p)) {
            return null;
        }

        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            return null;
        }
    }

    public boolean save(@NonNull String pathToFile, byte[] bytes) {
        throwIfEmpty(pathToFile, "pathToFile");

        Path p = Paths.get(pathToFile);
        if (Files.isDirectory(p)) {
            return false;
        }

        Path parent = p.getParent();
        if (parent != null) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                return false;
            }
        }

        try {
            Files.write(p, bytes);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public FileStat stat(@NonNull String path) {
        throwIfEmpty(path);

        Path p = Paths.get(path);
        FileStat fileStat = new FileStat();
        fileStat.path = path;
        if (Files.isRegularFile(p)) {
            fileStat.type = FileStat.TYPE_FILE;
            try {
                fileStat.contentLength = Files.size(p);
            } catch (IOException e) {
                return null;
            }
        } else if (Files.isDirectory(p)) {
            fileStat.type = FileStat.TYPE_DIRECTORY;
        } else {
            return null;
        }
        try {
            fileStat.lastModified = Files.getLastModifiedTime(p).toMillis();
        } catch (IOException e) {
            return null;
        }
        return fileStat;
    }
}

public class FileOps extends FileOpsCore {

    public static final FileOps singleton = new FileOps();

    /**
     * Starting from `pathPrefix`, list the next nodes in the file tree. `pathPrefix` need not be an existing file or directory.
     * `pathPrefix` might be empty.
     * Results are sorted. Directories end with `/`.
     * e.g.
     * - "d" => ["d/"]
     * - "d/" => ["d/d/", "d/f"]
     * - "f" => ["f"]
     * - "lo" => ["lo/", "lower/", "long"]
     * - "." => [".git/", ".gitignore", "...a"]
     * - ".." => ["...a"]
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
        List<String> a = listDirectory(d);
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
                .sorted(PathOps.singleton::compare)
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

    private List<Path> listDirectoryDepthFirstSearch(Path src, final int depth, AtomicBoolean abortRequested) {
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
                children.sort(Comparator.comparing(Path::toString, PathOps.singleton::compare));
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
    private List<Path> listDirectoryBreadthFirstSearch(Path src, int depth, AtomicBoolean abortRequested) {
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

    /**
     * Copy `src` to `dst`.
     * `src` must exist; `dst` must not exist and its parent must be a directory.
     * Not follow symlink.
     */
    public boolean copyDirectory(@NonNull String src, @NonNull String dst, AtomicBoolean abortRequested) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");

        return copyDirectory(Paths.get(src), Paths.get(dst), abortRequested);
    }

    /**
     * `src` must exist
     * `dst` must not exist but its parent must be directory
     */
    private boolean copyDirectory(Path src, Path dst, AtomicBoolean abortRequested) {
        if (!Files.exists(src) || Files.exists(dst)) {
            return false;
        }
        // a single-segment dst has no parent; treat that as the current directory
        Path dstParent = dst.getParent();
        if (dstParent == null) {
            dstParent = Paths.get(".");
        }
        if (!Files.isDirectory(dstParent)) {
            return false;
        }
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (Files.isSymbolicLink(src)) {
            // create a new symlink pointing to the same target
            try {
                Files.copy(src, dst, LinkOption.NOFOLLOW_LINKS);
                return true;
            } catch (IOException ignored) {
                return false;
            }
        } else if (Files.isDirectory(src)) {
            try {
                Files.createDirectory(dst);
            } catch (IOException ignored) {
                return false;
            }
            List<Path> children;
            try (Stream<Path> stream = Files.list(src)) {
                children = stream.collect(Collectors.toList());
            } catch (IOException ignored) {
                return false;
            }
            children.sort(Comparator.comparing(Path::toString, PathOps.singleton::compare));
            for (Path child : children) {
                if (abortRequested != null && abortRequested.get()) {
                    return false;
                }
                Path dstChild = dst.resolve(child.getFileName());
                if (!copyDirectory(child, dstChild, abortRequested)) {
                    return false;
                }
            }
            return true;
        } else {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            return copyFile(src, dst, abortRequested);
        }
    }

    /**
     * Copy the file `src` to `dst` by content.
     * `src` must exist; `dst` must not exist and its parent must be a directory.
     * `src` must be a file or a symlink to a file.
     * If aborted in halfway, the partially written `dst` is deleted.
     */
    public boolean copyFile(@NonNull String src, @NonNull String dst, AtomicBoolean abortRequested) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return copyFile(Paths.get(src), Paths.get(dst), abortRequested);
    }

    private boolean copyFile(Path src, Path dst, AtomicBoolean abortRequested) {
        if (!Files.exists(src) || !Files.isRegularFile(src) || Files.exists(dst) || (dst.getParent() != null && !Files.exists(dst.getParent()))) {
            // dst.getParent() == null: current directory
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }

        boolean succeeded = false;
        try (InputStream fis = Files.newInputStream(src);
             OutputStream fos = Files.newOutputStream(dst)) {
            byte[] a = new byte[8192];
            int nRead;
            while ((nRead = fis.read(a)) > 0) {
                if (abortRequested != null && abortRequested.get()) {
                    return false;
                }
                fos.write(a, 0, nRead);
            }
            fos.flush();
            succeeded = true;
            return true;
        } catch (IOException ignored) {
            return false;
        } finally {
            if (!succeeded) {
                try {
                    Files.deleteIfExists(dst);
                } catch (IOException ignored) {
                }
            }
        }
    }

    public String loadString(@NonNull String pathToFile) {
        byte[] a = load(pathToFile);
        if (a == null) {
            return null;
        }
        return new String(a, StandardCharsets.UTF_8);
    }

    public boolean saveString(@NonNull String pathToFile, String s) {
        return save(pathToFile, s.getBytes(StandardCharsets.UTF_8));
    }
}
