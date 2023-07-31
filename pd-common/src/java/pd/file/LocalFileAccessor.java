package pd.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LocalFileAccessor implements FileAccessor {

    private static final LocalFileAccessor one = new LocalFileAccessor();

    public static LocalFileAccessor singleton() {
        return one;
    }

    public boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    public boolean isDirectory(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    @Override
    public boolean isRegularFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }

    @Override
    public List<String> list2(String pathPrefix) {
        String path;
        if (pathPrefix.endsWith("/")) {
            path = pathPrefix;
        } else {
            int lastIndex = pathPrefix.lastIndexOf('/');
            if (lastIndex > 0) {
                path = pathPrefix.substring(0, lastIndex);
            } else {
                path = "";
            }
        }
        try (Stream<Path> stream = Files.list(Paths.get(path))) {
            return stream.map(a -> {
                        if (Files.isDirectory(a)) {
                            return a + "/";
                        } else {
                            return a.toString();
                        }
                    })
                    .filter(a -> a.startsWith(pathPrefix))
                    .sorted(PathExtension::compare)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<String> listAllRegularFiles(String path) {
        if (true) {
            return listRegularFiles(path, -1);
        }
        if (path.isEmpty()) {
            throw new IllegalArgumentException("`path` should not be empty");
        }

        List<String> l;
        {
            if (path.equals(".") || path.equals("./")) {
                l = list2("");
            } else if (path.endsWith("/")) {
                l = list2(path);
            } else {
                List<String> a = list2(path);
                if (a != null) {
                    if (a.contains(path)) {
                        // it is a file
                        return Collections.singletonList(path);
                    } else if (a.contains(path + "/")) {
                        l = list2(path + "/");
                    } else {
                        throw new IllegalArgumentException("`path` should identify a regular file or directory");
                    }
                } else {
                    throw new RuntimeException("failed to list files");
                }
            }
        }

        Queue<String> queue = new LinkedList<>(l);
        List<String> files = new LinkedList<>();
        while (!queue.isEmpty()) {
            String p = queue.poll();
            if (p.endsWith("/")) {
                l = list2(p);
                if (l == null) {
                    throw new RuntimeException("failed to list files");
                }
                queue.addAll(l);
            } else {
                files.add(p);
            }
        }
        return files.stream().sorted(PathExtension::compare).collect(Collectors.toList());
    }

    public List<String> listRegularFiles(String path, int depth) {
        if (!exists(path)) {
            return null;
        }
        if (isRegularFile(path)) {
            return Collections.singletonList(path);
        }
        if (depth == 0) {
            return Collections.emptyList();
        }
        if (isDirectory(path)) {
            List<String> paths = listDirectory2(path);
            if (depth == 1) {
                return paths.stream()
                        .filter(this::isRegularFile)
                        .sorted(PathExtension::compare)
                        .collect(Collectors.toList());
            } else {
                return paths.stream()
                        .flatMap(s -> listRegularFiles(s, depth - 1).stream())
                        .sorted(PathExtension::compare)
                        .collect(Collectors.toList());
            }
        }
        // ignore files of other types
        return Collections.emptyList();
    }

    /**
     * List the base names of directories and regular files directly under this directory.<br/>
     * Return null if `path` does not identify a directory.<br/>
     */
    public List<String> listDirectory(String path) {
        File f = new File(path);
        String[] names;
        try {
            names = f.list();
        } catch (Exception e) {
            return null;
        }
        if (names == null) {
            return null;
        }
        return Arrays.asList(names);
    }

    /**
     * List full paths of directories and regular files directly under this directory.<br/>
     * Return null if `path` does not identify a directory.<br/>
     */
    public List<String> listDirectory2(String path) {
        try (Stream<Path> stream = Files.list(Paths.get(path))) {
            return stream.map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    public List<FileStat> statAllRegularFiles(String path) {
        return listAllRegularFiles(path).stream().map(a -> {
            FileStat stat = new FileStat();
            stat.path = a;
            try {
                Path p = Paths.get(a);
                stat.size = Files.size(p);
                stat.lastModifiedTime = Files.getLastModifiedTime(p).toInstant().toEpochMilli();
            } catch (IOException e) {
                // dummy
            }
            return stat;
        }).collect(Collectors.toList());
    }

    /**
     * mkdir, mkdir -p
     */
    public boolean makeDirectory(String path, boolean parents) {
        try {
            Path p = Paths.get(path);
            if (!parents) {
                Files.createDirectory(p);
            } else {
                Files.createDirectories(p);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * rmdir, rmdir -p
     */
    public boolean removeDirectory(String path, boolean parents) {
        if (!isDirectory(path)) {
            return false;
        }
        List<String> paths = listDirectory2(path);
        if (paths == null || !paths.isEmpty()) {
            return false;
        }
        if (!remove(path, false)) {
            return false;
        }
        if (parents) {
            removeDirectory(PathExtension.dirname(path), true);
        }
        return true;
    }

    /**
     * rm -f, rm -rf
     */
    @Override
    public boolean remove(String path, boolean recursive) {
        if (!exists(path)) {
            return true;
        }
        if (isRegularFile(path)) {
            return removeRegularFile(path);
        } else if (isDirectory(path)) {
            if (!recursive) {
                return removeDirectory(path, false);
            }
            return removeDirectoryRecursively(path);
        }
        return false;
    }

    public boolean removeDirectoryRecursively(String path) {
        if (!exists(path)) {
            return true;
        }
        if (!isDirectory(path)) {
            return false;
        }
        for (String a : listDirectory2(path)) {
            if (!removeDirectoryRecursively(a)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return `true` if the file does not exist or is removed.<br/>
     */
    @Override
    public boolean removeRegularFile(String path) {
        if (!exists(path)) {
            return true;
        }
        if (!isRegularFile(path)) {
            return false;
        }
        try {
            Files.delete(Paths.get(path));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * cp f f1      // OK<br/>
     * cp -r f f1   // OK<br/>
     * cp d d1      // cp: -r not specified; omitting directory 'd'<br/>
     * cp -r d d1   // OK<br/>
     */
    @Override
    public boolean copy(String path, String dstPath, boolean recursive) {
        if (isRegularFile(path)) {
            try {
                Files.copy(Paths.get(path), Paths.get(dstPath));
                return true;
            } catch (IOException e) {
                return false;
            }
        } else if (isDirectory(path)) {
            if (!recursive) {
                return false;
            }
            List<String> names = listDirectory(path);
            for (String basename : names) {
                if (!copy(PathExtension.join(path, basename), PathExtension.join(dstPath, basename), true)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean move(String path, String dstPath) {
        if (isRegularFile(path)) {
            try {
                Files.move(Paths.get(path), Paths.get(dstPath));
                return true;
            } catch (IOException e) {
                return false;
            }
        } else if (isDirectory(path)) {
            List<String> names = listDirectory(path);
            for (String basename : names) {
                if (!move(PathExtension.join(path, basename), PathExtension.join(dstPath, basename))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public byte[] load(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            return null;
        }
    }

    public String loadText(String path) {
        byte[] bytes = load(path);
        if (bytes == null) {
            return null;
        }
        return new String(load(path));
    }

    @Override
    public boolean save(String path, byte[] bytes) {
        String dirname = PathExtension.dirname(path);
        if (!exists(dirname)) {
            if (!makeDirectory(dirname, true)) {
                return false;
            }
        }
        try {
            Files.write(Paths.get(path), bytes);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean saveText(String path, String s) {
        return save(path, s.getBytes(StandardCharsets.UTF_8));
    }
}
