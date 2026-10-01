package pd.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import lombok.NonNull;

import static pd.util.PathOps.throwIfEmpty;

/**
 * 目录树中只有两种结点：目录、文件。目录可以是叶结点，文件只能是叶结点。
 * 文件再分为常规文件和特殊文件。其中，符号链接影响了目录树的结构。
 * 基本操作集：枚举目录子结点(读目录)，创建/删除叶结点(写目录)，读/写文件，读/写结点属性。
 * 没有"原地编辑"符号链接内容的系统调用。因此，读写符号链接内容即为读写目标结点内容；读/写符号链接属性则与目标结点无关。
 * 日常操作默认跟随符号链接。
 * 应在每次变更性IO之前或循环开始时检查`abortRequested`。
 */
public class FileOps extends FileOpsInternal {

    public static final FileOps singleton = new FileOps();

    public FileStat stat(@NonNull String path) {
        throwIfEmpty(path);
        return stat(Paths.get(path));
    }

    public List<String> list(@NonNull String pathPrefix) {
        return list(pathPrefix, 1);
    }

    public List<String> list(@NonNull final String pathPrefix, int depth) {
        return list(pathPrefix, depth, true);
    }

    /**
     * List stop nodes matching `pathPrefix` up to `depth` in the file tree.
     * `pathPrefix` might be empty.
     * Return `null` if `pathPrefix` matches nothing.
     * Results are sorted.
     * Directories end with `/`.
     * e.g.
     * - "d" => ["d/"]
     * - "d/" => ["d/d/", "d/f"]
     * - "d/f" => []
     * - "lo" => ["lo/", "lower/", "long"]
     * - "." => [".git/", "...a", ".gitignore"]
     * - "./" => ["./d/", "./lo/", ...]
     * - ".." => ["...a"]
     * - "../" => ["../{name}/", ...]
     */
    public List<String> list(@NonNull final String pathPrefix, int depth, boolean followSymlinks) {
        if (depth < 1) {
            return null;
        }

        String d;
        if (pathPrefix.endsWith("/")) {
            d = pathPrefix;
        } else {
            // look up the capping directory
            int lastIndex = pathPrefix.lastIndexOf('/');
            if (lastIndex >= 0) {
                d = pathPrefix.substring(0, lastIndex + 1);
            } else {
                d = "";
            }
        }
        List<Path> a = listDirectory(Paths.get(d));
        if (a == null) {
            return null;
        }
        List<String> filtered = pathToStringAndFilterAndSort(a, followSymlinks).stream()
                .filter(s -> s.startsWith(pathPrefix) && !s.equals(pathPrefix))
                .collect(Collectors.toList());
        if (filtered.isEmpty()) {
            Path p = Paths.get(pathPrefix);
            boolean exists = followSymlinks
                    ? Files.exists(p)
                    : Files.exists(p, LinkOption.NOFOLLOW_LINKS);
            if (!exists) {
                return null;
            }
        }
        if (depth == 1) {
            return filtered;
        }

        List<String> results = new LinkedList<>();
        LinkedList<IntEntry<String>> stack = new LinkedList<>();
        // reversed order
        for (int i = filtered.size() - 1; i >= 0; i--) {
            stack.push(new IntEntry<>(depth - 1, filtered.get(i)));
        }
        while (!stack.isEmpty()) {
            IntEntry<String> frame = stack.pop();
            String s1 = frame.value;
            int depth1 = frame.key;
            if (depth1 == 0 || !s1.endsWith("/")) {
                results.add(s1);
                continue;
            }
            Path path1 = Paths.get(s1);
            List<Path> children2 = listDirectory(path1);
            if (children2 == null) {
                continue;
            }
            if (children2.isEmpty()) {
                results.add(s1);
                continue;
            }
            children2.stream()
                    .map(p -> pathToString(p, followSymlinks))
                    .filter(Objects::nonNull)
                    .sorted((x, y) -> -PathOps.singleton.compare(x, y))
                    .forEachOrdered(s -> stack.push(new IntEntry<>(depth1 - 1, s)));
        }
        return results;
    }

    /**
     * List `directory` down to `depth`.
     * `directory` must be a directory or a symlink to a directory.
     * `depth` should be positive.
     * Discovered directory/file is reported (directories end with "/") in pre-order.
     */
    public boolean listDirectory(@NonNull String directory, int depth, boolean followSymlinks, boolean sort,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(directory, "directory");
        Path src = Paths.get(directory);
        return sort
                ? listDirectorySorted(src, depth, followSymlinks, abortRequested, onAction)
                : listDirectory(src, depth, followSymlinks, abortRequested, onAction);
    }

    /**
     * `directory` must be a directory or a symlink to a directory.
     */
    public Long getNumDirectoryEntries(@NonNull String directory) {
        throwIfEmpty(directory, "directory");
        List<Path> paths = listDirectory(Paths.get(directory));
        if (paths == null) {
            return null;
        }
        return (long) paths.size();
    }

    /**
     * `path` must not exist but its parent must exist.
     * Does not follow symlinks.
     */
    public boolean createEmptyDirectory(@NonNull String path) {
        throwIfEmpty(path);
        return createEmptyDirectory(Paths.get(path));
    }

    /**
     * `pathToDirectory` must not exist.
     * Does not follow symlinks.
     * Fail-fast.
     * If aborted recognized, the `onAction` callback is not called.
     */
    public boolean createDirectory(@NonNull String pathToDirectory, boolean parents,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(pathToDirectory, "pathToDirectory");
        return createDirectory(Paths.get(pathToDirectory), parents, abortRequested, onAction);
    }

    /**
     * `path` must be an empty directory.
     * Does not follow symlinks.
     */
    public boolean removeEmptyDirectory(@NonNull String path) {
        throwIfEmpty(path);
        return removeEmptyDirectory(Paths.get(path));
    }

    /**
     * `pathToDirectory` must be a directory.
     * Does not follow symlinks.
     * Fail-fast.
     * If aborted recognized, the `onAction` callback is not called.
     */
    public boolean removeDirectory(@NonNull String pathToDirectory, boolean recursive, boolean parents,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(pathToDirectory, "pathToDirectory");
        return removeDirectory(Paths.get(pathToDirectory), recursive, parents, abortRequested, onAction);
    }

    /**
     * `pathToFile` must exist and must not be a directory.
     * Fails on special files.
     * Does not follow symlinks.
     */
    public boolean removeFile(@NonNull String pathToFile, OnActionListener onAction) {
        throwIfEmpty(pathToFile, "pathToFile");
        Path src = Paths.get(pathToFile);
        return removeFile(src, stat(src), onAction);
    }

    /**
     * `src` must be a directory.
     * `dst` must not exist but its parent must exist.
     * Does not follow symlinks.
     * Fail-fast.
     * If aborted recognized, the `onAction` callback is not called.
     */
    public boolean copyDirectory(@NonNull String src, @NonNull String dst,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return copyDirectory(Paths.get(src), Paths.get(dst), abortRequested, onAction);
    }

    /**
     * `src` must exist and must not be a directory.
     * `dst` must not exist but its parent must exist, without following symlinks.
     * Fails on special files.
     * If aborted recognized, the partially written `dst` is removed.
     */
    public boolean copyFile(@NonNull String src, @NonNull String dst, boolean followSymlinks,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return copyFile(Paths.get(src), Paths.get(dst), stat(src), followSymlinks, abortRequested, onAction);
    }

    /**
     * `src` must exist.
     * `dst` must not exist but its parent must exist.
     * Fails on special files.
     * Does not follow symlinks.
     */
    public boolean move(@NonNull String src, @NonNull String dst, OnActionListener onAction) {
        throwIfEmpty(src, "src");
        throwIfEmpty(dst, "dst");
        return move(Paths.get(src), Paths.get(dst), onAction);
    }

    /**
     * `pathToFile` must exist and be a regular file or a symlink to a regular file.
     */
    public byte[] load(@NonNull String pathToFile) {
        throwIfEmpty(pathToFile, "pathToFile");
        return load(Paths.get(pathToFile));
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
     * `pathToFile` must not exist.
     * Does not follow symlinks.
     */
    public boolean save(@NonNull String pathToFile, byte[] bytes, boolean parents) {
        throwIfEmpty(pathToFile, "pathToFile");
        return save(Paths.get(pathToFile), bytes, parents);
    }

    public boolean saveString(@NonNull String pathToFile, String s) {
        return save(pathToFile, s.getBytes(StandardCharsets.UTF_8));
    }

    public interface OnActionListener {
        void accept(Action action, String from, String to, Boolean succeeded);
    }

    /**
     * Event fires once per entry.
     * If failed, provides expected paths.
     */
    public enum Action {
        LIST,
        MEET, // as production of LIST
        CREATE, // a trailing "/" marks an actual or expected directory
        REMOVE, // a trailing "/" marks an actual or expected directory
        MOVE,
    }
}
