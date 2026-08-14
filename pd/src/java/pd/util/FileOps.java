package pd.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;

import static pd.util.PathOps.throwIfEmpty;

/**
 * 目录树中只有两种结点：目录、文件。目录可以是叶结点，文件只能是叶结点。
 * 文件再分为常规文件和特殊文件。其中，符号链接影响了目录树的结构。
 * 基本操作集：枚举目录子结点(读目录)，创建/删除叶结点(写目录)，读/写文件，读/写结点属性。
 * 没有"原地编辑"符号链接内容的系统调用。因此，读写符号链接内容即为读写目标结点内容；读/写符号链接属性则与目标结点无关。
 * 日常操作无需区分符号链接。
 */
class FileOpsCore {

    public List<String> list(@NonNull String pathPrefix) {
        return list(pathPrefix, 1);
    }

    /**
     * Starting from `pathPrefix`, list the next nodes in the file tree.
     * `pathPrefix` might be empty.
     * Results are sorted.
     * Directories end with `/`.
     * e.g.
     * - "d" => ["d/"]
     * - "d/" => ["d/d/", "d/f"]
     * - "f" => ["f"]
     * - "lo" => ["lo/", "lower/", "long"]
     * - "." => [".git/", ".gitignore", "...a"]
     * - ".." => ["...a"]
     */
    public List<String> list(@NonNull String pathPrefix, int depth) {
        if (depth < 1) {
            return null;
        }

        // look up the capping directory
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
        List<Path> a = listDirectory(Paths.get(d));
        if (a == null) {
            return Collections.emptyList();
        } else if (depth == 1) {
            return sortPaths(a).stream()
                    .map(this::pathToString)
                    .filter(s -> s.startsWith(pathPrefix))
                    .collect(Collectors.toList());
        }

        a = sortPaths(a).stream()
                .filter(p -> pathToString(p).startsWith(pathPrefix))
                .collect(Collectors.toList());

        List<String> results = new LinkedList<>();
        LinkedList<SimpleEntry<Path, Integer>> stack = new LinkedList<>();
        // reversed order
        for (int i = a.size() - 1; i >= 0; i--) {
            stack.push(new SimpleEntry<>(a.get(i), depth - 1));
        }
        while (!stack.isEmpty()) {
            SimpleEntry<Path, Integer> frame = stack.pop();
            Path path1 = frame.getKey();
            int depth1 = frame.getValue();
            if (depth1 == 0 || Files.isRegularFile(path1)) {
                results.add(pathToString(path1));
                continue;
            }
            List<Path> children;
            try (Stream<Path> stream = Files.list(path1)) {
                children = stream.collect(Collectors.toList());
            } catch (IOException e) {
                continue;
            }
            children = sortPaths(children);
            // reversed order
            for (int i = children.size() - 1; i >= 0; i--) {
                Path child = children.get(i);
                stack.push(new SimpleEntry<>(child, depth1 - 1));
            }
        }
        return results;
    }

    /**
     * `src` must be a directory or a symlink to a directory.
     * Results not sorted.
     * Follow symlink.
     */
    protected List<Path> listDirectory(Path src) {
        if (!Files.isDirectory(src)) {
            return null;
        }

        try (Stream<Path> stream = Files.list(src)) {
            return stream.collect(Collectors.toList());
        } catch (IOException ignored) {
            return null;
        }
    }

    protected String pathToString(Path p) {
        return PathOps.singleton.pathToString(p);
    }

    protected List<Path> sortPaths(List<Path> a) {
        a.sort(Comparator
                .<Path, Boolean>comparing(p -> !Files.isDirectory(p))
                .thenComparing(Path::toString, PathOps.singleton::compare));
        return a;
    }

    public List<String> listAll(@NonNull String pathPrefix) {
        return list(pathPrefix, Integer.MAX_VALUE);
    }

    public FileStat stat(@NonNull String path) {
        throwIfEmpty(path);

        Path src = Paths.get(path);
        BasicFileAttributes ownAttrs;
        try {
            ownAttrs = Files.readAttributes(src, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
        } catch (IOException e) {
            return null;
        }

        FileStat fileStat = new FileStat();
        fileStat.path = path;
        if (ownAttrs.isSymbolicLink()) {
            String targetType;
            try {
                BasicFileAttributes targetAttrs = Files.readAttributes(src, BasicFileAttributes.class);
                if (targetAttrs.isRegularFile()) {
                    targetType = FileStat.TYPE_FILE;
                } else if (targetAttrs.isDirectory()) {
                    targetType = FileStat.TYPE_DIRECTORY;
                } else {
                    targetType = FileStat.TYPE_UNKNOWN;
                }
            } catch (IOException e) {
                // broken symlink
                targetType = "";
            }
            fileStat.type = "l" + targetType;
            fileStat.contentLength = ownAttrs.size();
            fileStat.lastModified = ownAttrs.lastModifiedTime().toMillis();
        } else if (ownAttrs.isRegularFile()) {
            fileStat.type = FileStat.TYPE_FILE;
            fileStat.contentLength = ownAttrs.size();
            fileStat.lastModified = ownAttrs.lastModifiedTime().toMillis();
        } else if (ownAttrs.isDirectory()) {
            fileStat.type = FileStat.TYPE_DIRECTORY;
            fileStat.lastModified = ownAttrs.lastModifiedTime().toMillis();
        } else {
            fileStat.type = FileStat.TYPE_UNKNOWN;
            fileStat.lastModified = ownAttrs.lastModifiedTime().toMillis();
        }
        return fileStat;
    }
}

public class FileOps extends FileOpsCore {

    public static final FileOps singleton = new FileOps();

    /**
     * List offspring of `directory` down to `depth`.
     * `depth` should be positive.
     * Follow symlink.
     */
    public List<String> listDirectory(@NonNull String directory, int depth, AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(directory, "directory");

        List<Path> a = listDirectory(Paths.get(directory), depth, abortRequested, onAction);
        return a == null
                ? null
                : a.stream().map(Path::toString).collect(Collectors.toList());
    }

    private List<Path> listDirectory(Path src, final int depth, AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isDirectory(src)) {
            return null;
        }
        if (depth < 1) {
            return null;
        }

        LinkedList<Path> results = new LinkedList<>();
        List<Path> children = listDirectory(src);
        if (children != null) {
            for (Path child : sortPaths(children)) {
                if (abortRequested != null && abortRequested.get()) {
                    return null;
                }

                results.add(child);
                if (onAction != null) {
                    onAction.accept(Action.REACH, child, null, null);
                }
                if (Files.isDirectory(child)) {
                    List<Path> childResults = listDirectory(child, depth - 1, abortRequested, onAction);
                    if (childResults != null) {
                        results.addAll(childResults);
                    }
                }
            }
        }

        return results;
    }

    public boolean createDirectory(@NonNull String pathToDirectory, boolean parents, AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(pathToDirectory, "pathToDirectory");
        return createDirectory(Paths.get(pathToDirectory), parents, abortRequested, onAction);
    }

    public boolean createDirectory(Path src, boolean parents, AtomicBoolean abortRequested, OnActionListener onAction) {
        if (Files.exists(src, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }

        if (onAction != null) {
            onAction.accept(Action.CREATE, null, src, null);
        }

        boolean succeeded = true;

        if (parents) {
            Path parent = src.getParent();
            if (parent != null && !Files.exists(parent)) {
                if (abortRequested != null && abortRequested.get()) {
                    return false;
                }
                if (!createDirectory(parent, true, abortRequested, onAction)) {
                    if (abortRequested != null && abortRequested.get()) {
                        return false;
                    }
                    succeeded = false;
                }
            }
        }

        if (succeeded) {
            try {
                Files.createDirectory(src);
            } catch (IOException ignored) {
                succeeded = false;
            }
        }

        if (onAction != null) {
            onAction.accept(Action.CREATE, null, src, succeeded);
        }

        return succeeded;
    }

    /**
     * Remove the directory at `pathToDirectory`.
     * `pathToDirectory` must be a directory.
     * Not follow symlink.
     * No callback if abort recognized.
     * Callbacks paired if no abort.
     */
    public boolean removeDirectory(@NonNull String pathToDirectory, boolean recursive, boolean parents, AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(pathToDirectory, "pathToDirectory");
        return removeDirectory(Paths.get(pathToDirectory), recursive, parents, abortRequested, onAction);
    }

    private boolean removeDirectory(Path src, boolean recursive, boolean parents, AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }

        if (onAction != null) {
            onAction.accept(Action.REMOVE, src, null, null);
        }

        boolean succeeded = true;

        if (recursive) {
            List<Path> children = listDirectory(src);
            if (children != null) {
                for (Path child : sortPaths(children)) {
                    if (abortRequested != null && abortRequested.get()) {
                        return false;
                    }
                    if (Files.isSymbolicLink(child)) {
                        if (!removeFile(child, onAction)) {
                            succeeded = false;
                            break;
                        }
                    } else if (Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS)) {
                        if (!removeFile(child, onAction)) {
                            succeeded = false;
                            break;
                        }
                    } else if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        if (!removeDirectory(child, true, false, abortRequested, onAction)) {
                            if (abortRequested != null && abortRequested.get()) {
                                return false;
                            }
                            succeeded = false;
                            break;
                        }
                    } else {
                        if (onAction != null) {
                            onAction.accept(Action.REMOVE, child, null, null);
                            onAction.accept(Action.REMOVE, child, null, false);
                        }
                        succeeded = false;
                        break;
                    }
                }
            }
        }

        try {
            succeeded = succeeded && Files.deleteIfExists(src);
        } catch (IOException ignored) {
            succeeded = false;
        }

        if (onAction != null) {
            onAction.accept(Action.REMOVE, src, null, succeeded);
        }

        if (!succeeded) {
            return false;
        }

        if (parents) {
            Path parent = src.getParent();
            while (parent != null) {
                if (abortRequested != null && abortRequested.get()) {
                    return false;
                }
                if (!removeDirectory(parent, false, false, abortRequested, onAction)) {
                    break;
                }
                parent = parent.getParent();
            }
        }
        return true;
    }

    /**
     * `pathToFile` must be a regular file or a symlink.
     * Not follow symlink.
     */
    public boolean removeFile(@NonNull String pathToFile, OnActionListener onAction) {
        throwIfEmpty(pathToFile, "pathToFile");
        return removeFile(Paths.get(pathToFile), onAction);
    }

    private boolean removeFile(Path src, OnActionListener onAction) {
        if (!Files.isRegularFile(src, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(src)) {
            return false;
        }

        if (onAction != null) {
            onAction.accept(Action.REMOVE, src, null, null);
        }
        boolean succeeded;
        try {
            // can remove a symlink to a non-empty directory
            succeeded = Files.deleteIfExists(src);
        } catch (IOException ignored) {
            succeeded = false;
        }
        if (onAction != null) {
            onAction.accept(Action.REMOVE, src, null, succeeded);
        }
        return succeeded;
    }

    /**
     * `src` must be a directory.
     * `dst` must not exist but its parent must exist.
     * Not follow symlink.
     */
    public boolean copyDirectory(@NonNull String src, @NonNull String dst, AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return copyDirectory(Paths.get(src), Paths.get(dst), abortRequested, onAction);
    }

    private boolean copyDirectory(Path src, Path dst, AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        if (Files.exists(dst, LinkOption.NOFOLLOW_LINKS) || (dst.getParent() != null && !Files.exists(dst.getParent()))) {
            return false;
        }

        if (onAction != null) {
            onAction.accept(Action.COPY, src, dst, null);
        }

        boolean succeeded = createDirectory(dst, false, abortRequested, onAction);
        if (succeeded) {
            List<Path> children = listDirectory(src);
            if (children == null) {
                succeeded = false;
            } else {
                for (Path child : sortPaths(children)) {
                    if (abortRequested != null && abortRequested.get()) {
                        return false;
                    }

                    Path dstChild = dst.resolve(child.getFileName());
                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        succeeded = copyDirectory(child, dstChild, abortRequested, onAction);
                    } else if (Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS)) {
                        succeeded = copyFile(child, dstChild, abortRequested, onAction);
                    } else if (Files.isSymbolicLink(child)) {
                        // copy symlink itself
                        succeeded = false;
                        if (onAction != null) {
                            onAction.accept(Action.COPY, child, dstChild, null);
                        }
                        try {
                            Files.copy(child, dstChild, LinkOption.NOFOLLOW_LINKS);
                            succeeded = true;
                        } catch (IOException ignored) {
                        }
                        if (onAction != null) {
                            onAction.accept(Action.COPY, child, dstChild, succeeded);
                        }
                    } else {
                        if (onAction != null) {
                            onAction.accept(Action.COPY, child, dstChild, null);
                            onAction.accept(Action.COPY, child, dstChild, false);
                        }
                        succeeded = false;
                    }

                    if (!succeeded) {
                        if (abortRequested != null && abortRequested.get()) {
                            return false;
                        }
                        break;
                    }
                }
            }
        }

        if (onAction != null) {
            onAction.accept(Action.COPY, src, dst, succeeded);
        }

        return succeeded;
    }

    /**
     * `src` must be a regular file or a symlink to a regular file.
     * `dst` must not exist but its parent must exist.
     * If aborted in halfway, the partially written `dst` is deleted.
     * Follow symlink.
     */
    public boolean copyFile(@NonNull String src, @NonNull String dst, AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return copyFile(Paths.get(src), Paths.get(dst), abortRequested, onAction);
    }

    private boolean copyFile(Path src, Path dst, AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isRegularFile(src)) {
            return false;
        }
        if (Files.exists(dst, LinkOption.NOFOLLOW_LINKS) || (dst.getParent() != null && !Files.exists(dst.getParent()))) {
            // dst.getParent() == null: current directory
            return false;
        }

        if (onAction != null) {
            onAction.accept(Action.COPY, src, dst, null);
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
        } catch (IOException ignored) {
            // failure: report below
        } finally {
            if (!succeeded) {
                try {
                    Files.deleteIfExists(dst);
                } catch (IOException ignored) {
                }
            }
        }

        if (onAction != null) {
            onAction.accept(Action.COPY, src, dst, succeeded);
        }

        return succeeded;
    }

    /**
     * `src` must be a directory, must not be a symlink
     * `dst` must not exist but its parent must exist
     */
    public boolean moveDirectory(@NonNull String src, @NonNull String dst, AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return moveDirectory(Paths.get(src), Paths.get(dst), abortRequested, onAction);
    }

    public boolean moveDirectory(Path src, Path dst, AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        if (Files.exists(dst, LinkOption.NOFOLLOW_LINKS) || (dst.getParent() != null && !Files.exists(dst.getParent()))) {
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (onAction != null) {
            onAction.accept(Action.MOVE, src, dst, null);
        }
        try {
            Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE);
            if (onAction != null) {
                onAction.accept(Action.MOVE, src, dst, true);
            }
            return true;
        } catch (AtomicMoveNotSupportedException e) {
            if (!copyDirectory(src, dst, abortRequested, onAction)) {
                return false;
            }
            return removeDirectory(src, true, false, abortRequested, onAction);
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * `src` must be a file or a symlink.
     * `dst` must not exist but its parent must exist.
     * Not follow symlink.
     */
    public boolean moveFile(@NonNull String src, @NonNull String dst, AtomicBoolean abortRequested) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return moveFile(Paths.get(src), Paths.get(dst), abortRequested);
    }

    public boolean moveFile(Path src, Path dst, AtomicBoolean abortRequested) {
        if (!Files.isRegularFile(src, LinkOption.NOFOLLOW_LINKS) && !Files.isSymbolicLink(src)) {
            return false;
        }
        if (Files.exists(dst, LinkOption.NOFOLLOW_LINKS) || (dst.getParent() != null && !Files.exists(dst.getParent()))) {
            return false;
        }
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        try {
            Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * `pathToFile` must exist and be a regular file or a symlink to a regular file.
     */
    public byte[] load(@NonNull String pathToFile) {
        throwIfEmpty(pathToFile, "pathToFile");

        Path src = Paths.get(pathToFile);
        if (!Files.exists(src) || !Files.isRegularFile(src)) {
            return null;
        }

        try {
            return Files.readAllBytes(src);
        } catch (IOException e) {
            return null;
        }
    }

    public String loadString(@NonNull String pathToFile) {
        byte[] a = load(pathToFile);
        if (a == null) {
            return null;
        }
        return new String(a, StandardCharsets.UTF_8);
    }

    public boolean save(@NonNull String pathToFile, byte[] bytes) {
        return save(pathToFile, bytes, true);
    }

    /**
     * `pathToFile` must not exist or be a regular file or be a symlink to a regular file.
     */
    public boolean save(@NonNull String pathToFile, byte[] bytes, boolean parents) {
        throwIfEmpty(pathToFile, "pathToFile");

        Path dst = Paths.get(pathToFile);
        if (Files.exists(dst) && !Files.isRegularFile(dst)) {
            return false;
        }

        if (parents) {
            Path parent = dst.getParent();
            if (parent == null) {
                parent = Paths.get("");
            }
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                return false;
            }
        }

        try {
            Files.write(dst, bytes);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean saveString(@NonNull String pathToFile, String s) {
        return save(pathToFile, s.getBytes(StandardCharsets.UTF_8));
    }

    public interface OnActionListener {

        default void accept(Action action, Path src, Path dst, Boolean succeeded) {
            accept(action,
                    src == null ? null : src.toString(),
                    dst == null ? null : dst.toString(),
                    succeeded);
        }

        void accept(Action action, String from, String to, Boolean succeeded);
    }

    public enum Action {
        REACH,
        CREATE,
        REMOVE,
        COPY,
        MOVE,
    }
}
