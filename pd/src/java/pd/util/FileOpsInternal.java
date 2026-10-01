package pd.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryStream;
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

import lombok.NonNull;

/**
 * implemented with NIO
 */
class FileOpsInternal {

    protected static final int CODE_OK = 0;
    protected static final int CODE_PRECHECK_FAILED = 1;
    protected static final int CODE_FAILED = 2;
    protected static final int CODE_ABORTED = 8;

    /**
     * Returns a non-null FileStat object on legal path.
     * Does follow symlinks.
     */
    protected FileStat stat(@NonNull Path src) {
        FileStat result = new FileStat(src);
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

    /**
     * Results are not sorted.
     * Follow symlink.
     */
    protected List<Path> listDirectory(Path src) {
        boolean opened = false;
        List<Path> paths = null;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(src)) {
            opened = true;
            paths = new LinkedList<>();
            for (Path p : stream) {
                paths.add(p);
            }
        } catch (IOException e) {
            // on error resume on stream closing
            if (!opened) {
                return null;
            }
        }
        return paths;
    }

    protected boolean listDirectory(Path src, final int depth, boolean followSymlinks,
            AtomicBoolean abortRequested, FileOps.OnActionListener onAction) {
        if (!Files.isDirectory(src) || depth < 1) {
            if (onAction != null) {
                onAction.accept(FileOps.Action.LIST, src + "/", null, false);
            }
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }

        boolean opened = false;
        boolean succeeded = true; // protect from IOE when close stream
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(src)) {
            opened = true;
            if (onAction != null) {
                onAction.accept(FileOps.Action.LIST, src + "/", null, true);
            }
            for (Path p : directoryStream) {
                if (abortRequested != null && abortRequested.get()) {
                    succeeded = false;
                    break;
                }
                String s = pathToString(p, followSymlinks);
                if (s == null) {
                    continue;
                }
                if (onAction != null) {
                    onAction.accept(FileOps.Action.MEET, s, null, null);
                }
                if (depth > 1 && s.endsWith("/")) {
                    if (!listDirectory(Paths.get(s), depth - 1, followSymlinks, abortRequested, onAction)) {
                        succeeded = false;
                        break;
                    }
                }
            }
        } catch (IOException e) {
            // resume on stream closing error
            if (!opened && onAction != null) {
                onAction.accept(FileOps.Action.LIST, src + "/", null, false);
            }
        }
        return succeeded;
    }

    /**
     * A trailing "/" will be added for directory.
     */
    protected String pathToString(Path path, boolean followSymlinks) {
        FileStat fileStat = stat(path);
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

    protected boolean listDirectorySorted(Path src, final int depth, boolean followSymlinks,
            AtomicBoolean abortRequested, FileOps.OnActionListener onAction) {
        if (!Files.isDirectory(src) || depth < 1) {
            if (onAction != null) {
                onAction.accept(FileOps.Action.LIST, src + "/", null, false);
            }
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }

        List<Path> children = listDirectory(src);
        if (onAction != null) {
            onAction.accept(FileOps.Action.LIST, src + "/", null, children != null);
        }
        if (children == null) {
            // on error resume
            return true;
        }
        List<String> sorted = pathToStringAndFilterAndSort(children, followSymlinks);
        for (String s : sorted) {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            if (onAction != null) {
                onAction.accept(FileOps.Action.MEET, s, null, null);
            }
            if (depth > 1 && s.endsWith("/")) {
                if (!listDirectorySorted(Paths.get(s), depth - 1, followSymlinks, abortRequested, onAction)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected List<String> pathToStringAndFilterAndSort(List<Path> children, boolean followSymlinks) {
        return children.stream()
                .map(p -> pathToString(p, followSymlinks))
                .filter(Objects::nonNull)
                .sorted(PathOps.singleton::compare)
                .collect(Collectors.toList());
    }

    protected boolean createEmptyDirectory(Path src) {
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

    private boolean notExistsButParentExists(Path src) {
        if (Files.exists(src, LinkOption.NOFOLLOW_LINKS)) {
            return false;
        }
        Path parent = src.getParent();
        return parent == null || Files.exists(parent, LinkOption.NOFOLLOW_LINKS);
    }

    protected boolean createDirectory(Path src, boolean parents,
            AtomicBoolean abortRequested, FileOps.OnActionListener onAction) {
        if (Files.exists(src, LinkOption.NOFOLLOW_LINKS)) {
            if (onAction != null) {
                onAction.accept(FileOps.Action.CREATE, null, src + "/", false);
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
        final boolean succeeded = createEmptyDirectory(src);
        if (onAction != null) {
            onAction.accept(FileOps.Action.CREATE, null, src + "/", succeeded);
        }
        return succeeded;
    }

    protected boolean removeEmptyDirectory(Path src) {
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

    protected boolean removeDirectory(Path src, boolean recursive, boolean parents,
            AtomicBoolean abortRequested, FileOps.OnActionListener onAction) {
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS)) {
            if (onAction != null) {
                onAction.accept(FileOps.Action.REMOVE, src + "/", null, false);
            }
            return false;
        }

        if (recursive) {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            List<Path> children = listDirectory(src);
            if (onAction != null) {
                onAction.accept(FileOps.Action.LIST, src + "/", null, children != null);
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
                FileStat childStat = stat(child);
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
        final boolean succeeded = removeEmptyDirectory(src);
        if (onAction != null) {
            onAction.accept(FileOps.Action.REMOVE, src + "/", null, succeeded);
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

    // for removeDirectory to avoid a second stat
    protected boolean removeFile(Path src, FileStat srcStat, FileOps.OnActionListener onAction) {
        final int code = removeFile(src, srcStat);
        if (onAction != null) {
            onAction.accept(FileOps.Action.REMOVE, src.toString(), null, code == CODE_OK);
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

    protected boolean copyDirectory(Path src, Path dst,
            AtomicBoolean abortRequested, FileOps.OnActionListener onAction) {
        if (!Files.isDirectory(src, LinkOption.NOFOLLOW_LINKS) || !notExistsButParentExists(dst) || isDescendantOf(dst, src)) {
            if (onAction != null) {
                // aim to create a directory so add '/' suffix
                onAction.accept(FileOps.Action.CREATE, null, dst + "/", false);
            }
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        final boolean succeeded = createEmptyDirectory(dst);
        if (onAction != null) {
            onAction.accept(FileOps.Action.CREATE, null, dst + "/", succeeded);
        }
        if (!succeeded) {
            return false;
        }

        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        List<Path> children = listDirectory(src);
        if (onAction != null) {
            onAction.accept(FileOps.Action.LIST, src + "/", null, children != null);
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
            FileStat childStat = stat(child);
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

    private boolean isDescendantOf(Path path, Path ancestor) {
        Path realPath = getRealPath(path);
        Path readAncestor = getRealPath(ancestor);
        return realPath != null
                && readAncestor != null
                && !realPath.equals(readAncestor)
                && realPath.startsWith(readAncestor);
    }

    private Path getRealPath(Path path) {
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

    protected boolean copyFile(Path src, Path dst, FileStat srcStat, boolean followSymlinks,
            AtomicBoolean abortRequested, FileOps.OnActionListener onAction) {
        final int code = copyFile(src, dst, srcStat, followSymlinks, abortRequested);
        if (code == CODE_ABORTED) {
            return false;
        }
        if (onAction != null) {
            onAction.accept(FileOps.Action.CREATE, src.toString(), dst.toString(), code == CODE_OK);
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
            boolean aborted = false;
            try (FileChannel fci = FileChannel.open(src, StandardOpenOption.READ);
                 FileChannel fco = FileChannel.open(dst, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
                created = true;
                long position = 0;
                long size = fci.size();
                while (position < size) {
                    if (abortRequested != null && abortRequested.get()) {
                        aborted = true;
                        break;
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
            // checked outside the try block so a close()-time IOException can't mask an abort as CODE_FAILED
            if (aborted) {
                return CODE_ABORTED;
            }
            return stuffed ? CODE_OK : CODE_FAILED;
        } else {
            return CODE_FAILED;
        }
    }

    protected boolean move(Path src, Path dst, FileOps.OnActionListener onAction) {
        FileStat srcStat = stat(src);
        boolean succeeded = move(src, dst, srcStat) == CODE_OK;
        if (onAction != null) {
            String suffix = srcStat.isDirectory(false) ? "/" : "";
            onAction.accept(FileOps.Action.MOVE, src + suffix, dst + suffix, succeeded);
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

    protected byte[] load(Path src) {
        if (!Files.isRegularFile(src)) {
            return null;
        }

        try {
            return Files.readAllBytes(src);
        } catch (IOException e) {
            return null;
        }
    }

    protected boolean save(Path dst, byte[] bytes, boolean parents) {
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

        if (!notExistsButParentExists(dst)) {
            return false;
        }

        boolean created = false;
        try (OutputStream stream = Files.newOutputStream(dst, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            created = true;
            stream.write(bytes);
            return true;
        } catch (IOException e) {
            if (created) {
                try {
                    Files.deleteIfExists(dst);
                } catch (IOException ignored) {
                }
            }
            // should be more strict than RO
            return false;
        }
    }
}
