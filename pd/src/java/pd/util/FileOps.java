package pd.util;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
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
 * 日常操作默认跟随符号链接。
 */
class FileOpsCore {

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
     * Results not sorted.
     * Follow symlink.
     */
    protected List<Path> listDirectory(Path src) {
        try (Stream<Path> stream = Files.list(src)) {
            return stream.collect(Collectors.toList());
        } catch (IOException ignored) {
            return null;
        }
    }

    /**
     * A trailing "/" will be added for directory.
     */
    public String pathToString(Path path, boolean followSymlinks) {
        FileStat fileStat = stat(path.toString());
        if (followSymlinks && fileStat.isDanglingSymlink()) {
            return null;
        }
        String s = path.toString();
        if (fileStat.isDirectory(followSymlinks)) {
            if (!s.endsWith("/")) {
                s += "/";
            }
        }
        return s;
    }

    protected List<String> pathToStringAndFilterAndSort(List<Path> children, boolean followSymlinks) {
        return children.stream()
                .map(p -> pathToString(p, followSymlinks))
                .filter(Objects::nonNull)
                .sorted(PathOps.singleton::compare)
                .collect(Collectors.toList());
    }

    /**
     * `path` must not exist but its parent must exist.
     * Does not follow symlinks.
     */
    public boolean createEmptyDirectory(@NonNull String path) {
        throwIfEmpty(path);
        Path src = Paths.get(path);
        if (!notExistsButParentExists(src)) {
            return false;
        }
        try {
            Files.createDirectory(src);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * `path` must be an empty directory.
     * Does not follow symlinks.
     */
    public boolean removeEmptyDirectory(@NonNull String path) {
        throwIfEmpty(path);
        Path src = Paths.get(path);
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        try {
            Files.delete(src);
            return true;
        } catch (IOException ignored) {
            return false;
        }
    }

    /**
     * Returns a non-null FileStat object on legal path.
     * Does follow symlinks.
     */
    public FileStat stat(@NonNull String path) {
        throwIfEmpty(path);

        FileStat result = new FileStat(path);

        Path src = Paths.get(path);
        BasicFileAttributes ownAttrs;
        try {
            ownAttrs = readAttributes(src, false);
        } catch (IOException e) {
            return result;
        }

        if (ownAttrs.isSymbolicLink()) {
            int targetType;
            try {
                BasicFileAttributes targetAttrs = readAttributes(src, true);
                if (targetAttrs.isRegularFile()) {
                    targetType = FileStat.TYPE_FILE;
                } else if (targetAttrs.isDirectory()) {
                    targetType = FileStat.TYPE_DIRECTORY;
                } else {
                    targetType = FileStat.TYPE_SPECIAL;
                }
            } catch (IOException e) {
                // dangling symlink
                targetType = 0;
            }
            result.type = FileStat.TYPE_SYMLINK | targetType;
            result.size = ownAttrs.size();
            result.mtime = ownAttrs.lastModifiedTime().toMillis();
        } else if (ownAttrs.isRegularFile()) {
            result.type = FileStat.TYPE_FILE;
            result.size = ownAttrs.size();
            result.mtime = ownAttrs.lastModifiedTime().toMillis();
        } else if (ownAttrs.isDirectory()) {
            result.type = FileStat.TYPE_DIRECTORY;
            result.mtime = ownAttrs.lastModifiedTime().toMillis();
        } else {
            result.type = FileStat.TYPE_SPECIAL;
            result.mtime = ownAttrs.lastModifiedTime().toMillis();
        }
        return result;
    }

    private BasicFileAttributes readAttributes(Path path, boolean followSymlinks) throws IOException {
        return followSymlinks
                ? Files.readAttributes(path, BasicFileAttributes.class)
                : Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS);
    }

    protected boolean notExistsButParentExists(Path src) {
        if (Files.exists(src, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        Path parent = src.getParent();
        return parent == null || Files.exists(parent, LinkOption.NOFOLLOW_LINKS);
    }
}

/**
 * 应在每次变更性IO之前或循环开始时检查`abortRequested`
 */
public class FileOps extends FileOpsCore {

    protected static final int CODE_OK = 0;
    protected static final int CODE_PRECHECK_FAILED = 1;
    protected static final int CODE_FAILED = 2;
    protected static final int CODE_ABORTED = 8;

    public static final FileOps singleton = new FileOps();

    /**
     * List `directory` down to `depth`.
     * `directory` must be a directory or a symlink to a directory.
     * `depth` should be positive.
     * Discovered directory/file is reported in pre-order; Directories end with "/".
     */
    public boolean listDirectory(@NonNull String directory, int depth, boolean followSymlinks,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        throwIfEmpty(directory, "directory");
        return listDirectory(Paths.get(directory), depth, followSymlinks, abortRequested, onAction);
    }

    protected boolean listDirectory(Path src, final int depth, boolean followSymlinks,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isDirectory(src) || depth < 1) {
            if (onAction != null) {
                onAction.accept(Action.LIST, src + "/", null, false);
            }
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        List<Path> children = listDirectory(src);
        if (onAction != null) {
            onAction.accept(Action.LIST, src + "/", null, children != null);
        }
        // continue on error
        if (children == null) {
            return true;
        }
        List<String> sorted = pathToStringAndFilterAndSort(children, followSymlinks);
        for (String s : sorted) {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            if (onAction != null) {
                onAction.accept(Action.MEET, s, null, null);
            }
            if (depth > 1 && s.endsWith("/")) {
                if (!listDirectory(Paths.get(s), depth - 1, followSymlinks, abortRequested, onAction)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * `directory` must be a directory or a symlink to a directory.
     */
    public Long countDirectoryDirectEntries(@NonNull String directory) {
        throwIfEmpty(directory, "directory");

        Path src = Paths.get(directory);
        try (Stream<Path> stream = Files.list(src)) {
            return stream.count();
        } catch (IOException e) {
            return null;
        }
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

    protected boolean createDirectory(Path src, boolean parents,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        if (Files.exists(src, LinkOption.NOFOLLOW_LINKS)) {
            if (onAction != null) {
                onAction.accept(Action.CREATE, null, src + "/", false);
            }
            return false;
        }

        if (parents) {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            Path parent = src.getParent();
            if (parent != null && !Files.exists(parent)) {
                if (!createDirectory(parent, true, abortRequested, onAction)) {
                    return false;
                }
            }
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        final boolean succeeded = createEmptyDirectory(src.toString());
        if (onAction != null) {
            onAction.accept(Action.CREATE, null, src + "/", succeeded);
        }
        return succeeded;
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

    protected boolean removeDirectory(Path src, boolean recursive, boolean parents,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS)) {
            if (onAction != null) {
                onAction.accept(Action.REMOVE, src + "/", null, false);
            }
            return false;
        }

        if (recursive) {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            List<Path> children = listDirectory(src);
            if (onAction != null) {
                onAction.accept(Action.LIST, src + "/", null, children != null);
            }
            if (children == null) {
                return false;
            }
            List<String> sorted = pathToStringAndFilterAndSort(children, false);
            for (String s : sorted) {
                if (abortRequested != null && abortRequested.get()) {
                    return false;
                }
                Path child = Paths.get(s);
                FileStat childStat = stat(s);
                if (childStat.isDirectory(false)) {
                    if (!removeDirectory(child, true, false, abortRequested, onAction)) {
                        return false;
                    }
                } else {
                    if (!removeFile(child, childStat, onAction)) {
                        return false;
                    }
                }
            }
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        final boolean succeeded = removeEmptyDirectory(src.toString());
        if (onAction != null) {
            onAction.accept(Action.REMOVE, src + "/", null, succeeded);
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
     * `pathToFile` must exist and must not be a directory.
     * Fails on special files.
     * Does not follow symlinks.
     */
    public boolean removeFile(@NonNull String pathToFile, OnActionListener onAction) {
        throwIfEmpty(pathToFile, "pathToFile");
        return removeFile(Paths.get(pathToFile), stat(pathToFile), onAction);
    }

    // for removeDirectory to avoid a second stat
    private boolean removeFile(Path src, FileStat srcStat, OnActionListener onAction) {
        final int code = removeFile(src, srcStat);
        if (onAction != null) {
            onAction.accept(Action.REMOVE, src.toString(), null, code == CODE_OK);
        }
        return code == CODE_OK;
    }

    private int removeFile(Path src, FileStat srcStat) {
        if (!srcStat.exists(false) || srcStat.isDirectory(false)) {
            return CODE_PRECHECK_FAILED;
        }
        if (srcStat.isAnyTypeOf("f", "l*")) {
            try {
                // it accepts symlink
                Files.delete(src);
                return CODE_OK;
            } catch (IOException ignored) {
                return CODE_FAILED;
            }
        }
        return CODE_FAILED;
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

    protected boolean copyDirectory(Path src, Path dst,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS) || !notExistsButParentExists(dst) || isDescendantOf(dst, src)) {
            if (onAction != null) {
                // aim to create a directory so add '/' suffix
                onAction.accept(Action.CREATE, null, dst + "/", false);
            }
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        final boolean succeeded = createEmptyDirectory(dst.toString());
        if (onAction != null) {
            onAction.accept(Action.CREATE, null, dst + "/", succeeded);
        }
        if (!succeeded) {
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        List<Path> children = listDirectory(src);
        if (onAction != null) {
            onAction.accept(Action.LIST, src + "/", null, children != null);
        }
        if (children == null) {
            return false;
        }
        List<String> sorted = pathToStringAndFilterAndSort(children, false);
        for (String s : sorted) {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            Path child = Paths.get(s);
            FileStat childStat = stat(s);
            Path dstChild = dst.resolve(child.getFileName());
            if (childStat.isDirectory(false)) {
                if (!copyDirectory(child, dstChild, abortRequested, onAction)) {
                    return false;
                }
            } else {
                if (!copyFile(child, dstChild, childStat, false, abortRequested, onAction)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean isDescendantOf(Path path, Path ancestor) {
        Path realPath = getRealPath(path);
        Path readAncestor = getRealPath(ancestor);
        return realPath != null
                && readAncestor != null
                && !realPath.equals(readAncestor)
                && realPath.startsWith(readAncestor);
    }

    protected Path getRealPath(Path path) {
        Path p = path.toAbsolutePath().normalize();
        List<Path> a = new LinkedList<>();
        while (!Files.exists(p, LinkOption.NOFOLLOW_LINKS)) {
            Path parent = p.getParent();
            if (parent == null) {
                return null;
            }
            a.add(0, p.getFileName());
            p = parent;
        }

        try {
            p = p.toRealPath();
        } catch (IOException ignored) {
            return null;
        }
        for (Path name : a) {
            p = p.resolve(name);
        }
        return p;
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

    private boolean copyFile(Path src, Path dst, FileStat srcStat, boolean followSymlinks,
            AtomicBoolean abortRequested, OnActionListener onAction) {
        final int code = copyFile(src, dst, srcStat, followSymlinks, abortRequested);
        if (code == CODE_ABORTED) {
            return false;
        }
        if (onAction != null) {
            onAction.accept(Action.CREATE, src.toString(), dst.toString(), code == CODE_OK);
        }
        return code == CODE_OK;
    }

    private int copyFile(Path src, Path dst, FileStat srcStat, boolean followSymlinks,
            AtomicBoolean abortRequested) {
        if (!srcStat.exists(followSymlinks) || srcStat.isDirectory(followSymlinks)) {
            return CODE_PRECHECK_FAILED;
        }
        if (!notExistsButParentExists(dst)) {
            return CODE_PRECHECK_FAILED;
        }

        if (srcStat.isSymlink() && !followSymlinks) {
            // copy symlink itself, atomic
            if (abortRequested != null && abortRequested.get()) {
                return CODE_ABORTED;
            }
            try {
                Files.copy(src, dst, LinkOption.NOFOLLOW_LINKS);
                return CODE_OK;
            } catch (IOException ignored) {
                return CODE_FAILED;
            }
        } else if (srcStat.isFile(true)) {
            // copy file content
            if (abortRequested != null && abortRequested.get()) {
                return CODE_ABORTED;
            }
            boolean created = false;
            boolean stuffed = false;
            try (FileChannel fci = FileChannel.open(src, StandardOpenOption.READ);
                 FileChannel fco = FileChannel.open(dst, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                created = true;
                long position = 0;
                long size = fci.size();
                while (position < size) {
                    if (abortRequested != null && abortRequested.get()) {
                        return CODE_ABORTED;
                    }
                    long n = fci.transferTo(position, Math.min(size - position, 4 * 1024 * 1024), fco);
                    if (n <= 0) {
                        break;
                    }
                    position += n;
                }
                stuffed = position == size;
            } catch (IOException ignored) {
            } finally {
                if (created && !stuffed) {
                    try {
                        Files.delete(dst);
                    } catch (IOException ignored) {
                    }
                }
            }
            return stuffed ? CODE_OK : CODE_FAILED;
        } else {
            return CODE_FAILED;
        }
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

    protected boolean move(Path src, Path dst, OnActionListener onAction) {
        FileStat srcStat = stat(src.toString());
        boolean succeeded = move(src, dst, srcStat) == CODE_OK;
        if (onAction != null) {
            String suffix = srcStat.isDirectory(false) ? "/" : "";
            onAction.accept(Action.MOVE, src + suffix, dst + suffix, succeeded);
        }
        return succeeded;
    }

    private int move(Path src, Path dst, FileStat srcStat) {
        if (!srcStat.exists(false) || !notExistsButParentExists(dst)) {
            return CODE_PRECHECK_FAILED;
        }
        if (srcStat.isAnyTypeOf("d", "f", "l*")) {
            try {
                // not follow symlinks
                Files.move(src, dst, StandardCopyOption.ATOMIC_MOVE);
                return CODE_OK;
            } catch (IOException ignored) {
                return CODE_FAILED;
            }
        }
        return CODE_FAILED;
    }

    /**
     * `pathToFile` must exist and be a regular file or a symlink to a regular file.
     */
    public byte[] load(@NonNull String pathToFile) {
        throwIfEmpty(pathToFile, "pathToFile");

        Path src = Paths.get(pathToFile);
        if (!Files.isRegularFile(src)) {
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
            if (parent != null) {
                try {
                    Files.createDirectories(parent);
                } catch (IOException e) {
                    return false;
                }
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
