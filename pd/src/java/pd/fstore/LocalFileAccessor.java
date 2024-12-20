package pd.fstore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pd.util.PathExtension;

public class LocalFileAccessor implements FileAccessor {

    @Override
    public List<String> list(String keyPrefix) {
        String parent = getParentDirectory(keyPrefix);
        try (Stream<Path> stream = Files.list(Paths.get(parent))) {
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

    private String getParentDirectory(String keyPrefix) {
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
        List<String> paths = list(keyPrefix);
        if (paths == null) {
            return null;
        }
        for (String path : paths) {
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

    @Override
    public boolean remove(String key) {
        return new File(key).delete();
    }

    @Override
    public boolean removeAll(String keyPrefix) {
        List<String> paths = list(keyPrefix);
        if (paths == null) {
            return false;
        }

        List<File> files = paths.stream().map(File::new).collect(Collectors.toList());
        while (!files.isEmpty()) {
            File f = files.remove(0);
            if (f.isDirectory()) {
                File[] fSubFiles = f.listFiles();
                if (fSubFiles == null || fSubFiles.length == 0) {
                    if (!f.delete()) {
                        return false;
                    }
                } else {
                    files.add(0, f);
                    files.addAll(0, Arrays.asList(fSubFiles));
                }
            } else if (f.exists()) {
                if (!f.delete()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public byte[] load(String key) {
        Path p = Paths.get(key);
        if (!Files.exists(p)) {
            return null;
        }
        if (Files.isDirectory(p)) {
            return null;
        }

        try {
            return Files.readAllBytes(p);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public boolean save(String key, byte[] bytes) {
        Path p = Paths.get(key);
        if (Files.exists(p) && Files.isDirectory(p)) {
            return false;
        }

        try {
            Files.createDirectories(p.getParent());
        } catch (IOException e) {
            return false;
        }
        try {
            Files.write(p, bytes);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public boolean exists(String key) {
        return stat(key) != null;
    }

    public String loadString(String key) {
        byte[] a = load(key);
        if (a == null) {
            return null;
        }
        return new String(a, StandardCharsets.UTF_8);
    }

    public boolean saveString(String key, String s) {
        return save(key, s.getBytes(StandardCharsets.UTF_8));
    }
}
