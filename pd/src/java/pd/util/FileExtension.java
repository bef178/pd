package pd.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.NonNull;
import lombok.SneakyThrows;

public class FileExtension {

    public static List<File> listDirectory(@NonNull File src) {
        if (!src.isDirectory()) {
            return null;
        }
        File[] children = src.listFiles();
        if (children == null) {
            return null;
        }
        return Arrays.asList(children);
    }

    /**
     * `src` must exist<br/>
     * `dst` must not exist<br/>
     */
    public static boolean copyRecursively(@NonNull File src, @NonNull File dst, AtomicBoolean abortRequested) {
        if (!src.exists() || dst.exists()) {
            return false;
        }
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (src.isDirectory()) {
            if (!dst.mkdirs()) {
                return false;
            }
            String[] children = src.list();
            if (children != null) {
                for (String child : children) {
                    if (abortRequested != null && abortRequested.get()) {
                        return false;
                    }
                    File srcChild = new File(src, child);
                    File dstChild = new File(dst, child);
                    if (!copyRecursively(srcChild, dstChild, abortRequested)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            if (abortRequested != null && abortRequested.get()) {
                return false;
            }
            try (FileInputStream srcStream = new FileInputStream(src);
                 FileOutputStream dstStream = new FileOutputStream(dst)) {
                byte[] buffer = new byte[8192];
                int nRead;
                while ((nRead = srcStream.read(buffer)) > 0) {
                    if (abortRequested != null && abortRequested.get()) {
                        boolean ignored = dst.delete();
                        return false;
                    }
                    dstStream.write(buffer, 0, nRead);
                }
                return true;
            } catch (IOException ignored) {
            }
            return false;
        }
    }

    /**
     * `src` must exist<br/>
     * `dst` must not exist<br/>
     */
    public static boolean moveRecursively(@NonNull File src, @NonNull File dst, AtomicBoolean abortRequested) {
        if (!src.exists() || dst.exists()) {
            return false;
        }
        // TODO check cross-filesystem and take use of `abortRequested`
        return src.renameTo(dst);
    }

    public static boolean removeRecursively(@NonNull File src, AtomicBoolean abortRequested) {
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (src.isDirectory()) {
            File[] children = src.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (abortRequested != null && abortRequested.get()) {
                        return false;
                    }
                    if (!removeRecursively(child, abortRequested)) {
                        return false;
                    }
                }
            }
        }
        return src.delete();
    }

    public static String pathToString(Path p) {
        String s = p.toString();
        if (s.startsWith("./")) {
            s = s.substring(2);
        }
        if (Files.isDirectory(p)) {
            s += "/";
        }
        return s;
    }

    public static List<Path> listDirectory(@NonNull Path src) {
        if (!Files.isDirectory(src)) {
            return null;
        }
        try (Stream<Path> stream = Files.list(src)) {
            return stream.collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    public static List<Path> listDirectory(@NonNull Path src, int depth, AtomicBoolean abortRequested) {
        return listDirectoryDepthFirstSearch(src, depth, abortRequested);
    }

    /**
     * List offspring of the given directory up to specified depth.<br/>
     * `depth` should be positive.<br/>
     * - Leading "./" will be trimmed.<br/>
     * - A trailing "/" will be present for directory.<br/>
     */
    private static List<Path> listDirectoryDepthFirstSearch(@NonNull Path src, final int depth, AtomicBoolean abortRequested) {
        if (!Files.isDirectory(src)) {
            return null;
        }
        if (abortRequested != null && abortRequested.get()) {
            return null;
        }
        LinkedList<Path> results = new LinkedList<>();
        if (depth > 0) {
            List<Path> children = listDirectory(src);
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
    private static List<Path> listDirectoryBreadthFirstSearch(@NonNull Path src, int depth, AtomicBoolean abortRequested) {
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

    public static boolean removeRecursively(@NonNull Path src, AtomicBoolean abortRequested) {
        if (abortRequested != null && abortRequested.get()) {
            return false;
        }
        if (Files.isDirectory(src)) {
            List<Path> children = listDirectory(src);
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
