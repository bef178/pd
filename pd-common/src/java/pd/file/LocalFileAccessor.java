package pd.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pd.util.PathExtension;

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
    public List<String> list(String keyPrefix) {
        String s = getUpDirectory(keyPrefix);
        try (Stream<Path> stream = Files.list(Paths.get(s))) {
            return stream
                    .map(a -> {
                        String aString = a.toString();
                        if (Files.isDirectory(a)) {
                            if (aString.startsWith("./")) {
                                return aString.substring(2) + '/';
                            } else {
                                return aString + "/";
                            }
                        } else {
                            if (aString.startsWith("./")) {
                                return aString.substring(2);
                            } else {
                                return aString;
                            }
                        }
                    })
                    .filter(a -> a.startsWith(keyPrefix))
                    .sorted(PathExtension::compare)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    private String getUpDirectory(String keyPrefix) {
        String s;
        if (keyPrefix.equals(".")) {
            s = "";
        } else if (keyPrefix.equals("./")) {
            s = "";
        } else if (keyPrefix.equals("..")) {
            s = "../";
        } else if (keyPrefix.endsWith("/")) {
            s = keyPrefix;
        } else {
            int lastIndex = keyPrefix.lastIndexOf('/');
            if (lastIndex >= 0) {
                s = keyPrefix.substring(0, lastIndex + 1);
            } else {
                s = "";
            }
        }
        return s;
    }

    @Override
    public List<String> listAll(String keyPrefix) {
        List<String> keys = new LinkedList<>();
        List<File> directories = new LinkedList<>();
        for (String path : list(keyPrefix)) {
            File f = new File(path);
            if (f.isDirectory()) {
                directories.add(f);
            } else {
                keys.add(path);
            }
        }
        while (!directories.isEmpty()) {
            File f = directories.remove(0);
            File[] subFiles = f.listFiles();
            if (subFiles != null) {
                for (File f1 : subFiles) {
                    if (f1.isDirectory()) {
                        directories.add(0, f1);
                    } else {
                        keys.add(0, f1.getPath());
                    }
                }
            }
        }
        return keys.stream().sorted(PathExtension::compare).collect(Collectors.toList());
    }

    @Override
    public FileStat stat(String key) {
        File f = new File(key);
        if (!f.isFile()) {
            return null;
        }
        FileStat fileStat = new FileStat();
        fileStat.key = key;
        fileStat.contentLength = f.length();
        fileStat.lastModified = f.lastModified();
        return fileStat;
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
        return listAll(path).stream().map(a -> {
            FileStat stat = new FileStat();
            stat.key = a;
            try {
                Path p = Paths.get(a);
                stat.contentLength = Files.size(p);
                stat.lastModified = Files.getLastModifiedTime(p).toInstant().toEpochMilli();
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
