package pd.fstore;

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

import lombok.NonNull;
import pd.util.PathExtension;

public class LocalFileAccessor implements FileAccessor {

    @Override
    public List<String> list(String prefix) {
        String d;
        if (prefix.equals(".")) {
            d = "";
        } else if (prefix.equals("./")) {
            d = "";
        } else if (prefix.equals("..")) {
            d = "../";
        } else if (prefix.endsWith("/")) {
            d = prefix;
        } else {
            int lastIndex = prefix.lastIndexOf('/');
            if (lastIndex >= 0) {
                d = prefix.substring(0, lastIndex + 1);
            } else {
                d = "";
            }
        }
        List<Path> a = listDirectory(Paths.get(d));
        if (a != null) {
            return a.stream()
                    .map(this::pathToString)
                    .filter(s1 -> s1.startsWith(prefix))
                    .sorted(PathExtension::compare)
                    .collect(Collectors.toList());
        }
        return null;
    }

    private LinkedList<Path> listDirectory(Path path) {
        try (Stream<Path> stream = Files.list(path)) {
            return stream.collect(Collectors.toCollection(LinkedList::new));
        } catch (IOException ignored) {
        }
        return null;
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

    @Override
    public List<String> listAll(@NonNull String prefix) {
        List<String> a = list(prefix);
        if (a == null) {
            return null;
        }
        return a.stream()
                .flatMap(s -> {
                    Path p = Paths.get(s);
                    if (Files.isDirectory(p)) {
                        LinkedList<Path> a1 = listDirectoryDepthFirstSearch(p, Integer.MAX_VALUE);
                        if (a1 == null) {
                            a1 = new LinkedList<>();
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
     * List offspring of the given directory up to specified depth.<br/>
     * `depth` should be positive.<br/>
     * - Leading "./" will be trimmed.<br/>
     * - A trailing "/" will be present for directory.<br/>
     */
    private LinkedList<Path> listDirectoryDepthFirstSearch(@NonNull Path p, final int depth) {
        if (!Files.isDirectory(p)) {
            return null;
        }

        LinkedList<Path> results = new LinkedList<>();
        if (depth > 0) {
            LinkedList<Path> a1 = listDirectory(p);
            if (a1 != null) {
                for (Path p1 : a1) {
                    results.add(p1);
                    if (Files.isDirectory(p1)) {
                        LinkedList<Path> a2 = listDirectoryDepthFirstSearch(p1, depth - 1);
                        if (a2 != null) {
                            results.addAll(a2);
                        }
                    }
                }
            }
        }
        return results;
    }

    private List<Path> listDirectoryBreadthFirstSearch(@NonNull Path p, int depth) {
        if (!Files.isDirectory(p)) {
            return null;
        }

        List<Path> results = new LinkedList<>();
        List<Path> thisQ = new LinkedList<>();
        thisQ.add(p);
        while (depth != 0) {
            List<Path> nextQ = new LinkedList<>();
            while (!thisQ.isEmpty()) {
                Path first = thisQ.remove(0);
                try (Stream<Path> stream = Files.list(first)) {
                    stream.forEach(p1 -> {
                        results.add(p1);
                        if (Files.isDirectory(p1)) {
                            nextQ.add(p1);
                        }
                    });
                } catch (IOException ignored) {
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

    @Override
    public boolean removeAll(@NonNull String prefix) {
        List<String> paths = list(prefix);
        if (paths == null) {
            return false;
        }

        for (String path : paths) {
            Path p = Paths.get(path);
            if (Files.isDirectory(p)) {
                removeDirectoryDepthFirstSearch(Paths.get(path));
                new File(path).delete();
            } else {
                remove(path);
            }
        }
        return true;
    }

    private LinkedList<Path> removeDirectoryDepthFirstSearch(@NonNull Path p) {
        if (!Files.isDirectory(p)) {
            return null;
        }

        LinkedList<Path> results = new LinkedList<>();
        LinkedList<Path> a1 = listDirectory(p);
        if (a1 != null) {
            for (Path p1 : a1) {
                if (Files.isDirectory(p1)) {
                    LinkedList<Path> a2 = removeDirectoryDepthFirstSearch(p1);
                    if (a2 != null) {
                        results.addAll(a2);
                    }
                    new File(p1.toString()).delete();
                    results.add(p1);
                } else {
                    remove(p1.toString());
                }
            }
        }
        return results;
    }

    @Override
    public FileStat stat(@NonNull String key) {
        File f = new File(key);
        FileStat fileStat = new FileStat();
        fileStat.key = key;
        if (f.isFile()) {
            fileStat.type = FileStat.TYPE_FILE;
            fileStat.contentLength = f.length();
        } else if (f.isDirectory()) {
            fileStat.type = FileStat.TYPE_DIRECTORY_LIKE;
        } else {
            return null;
        }
        fileStat.lastModified = f.lastModified();
        return fileStat;
    }

    @Override
    public boolean remove(@NonNull String key) {
        return new File(key).delete();
    }

    @Override
    public byte[] load(@NonNull String key) {
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
    public boolean save(@NonNull String key, byte[] bytes) {
        Path p = Paths.get(key);
        if (Files.exists(p) && Files.isDirectory(p)) {
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

    public boolean exists(@NonNull String key) {
        return new File(key).exists();
    }

    public String loadString(@NonNull String key) {
        byte[] a = load(key);
        if (a == null) {
            return null;
        }
        return new String(a, StandardCharsets.UTF_8);
    }

    public boolean saveString(@NonNull String key, String s) {
        return save(key, s.getBytes(StandardCharsets.UTF_8));
    }
}
