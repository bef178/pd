package pd.file;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileAccessor implements IFileAccessor {

    private static final FileAccessor one = new FileAccessor();

    public static FileAccessor singleton() {
        return one;
    }

    @Override
    public boolean exists(String path) {
        return Files.exists(Paths.get(path));
    }

    @Override
    public boolean isDirectory(String path) {
        return Files.isDirectory(Paths.get(path));
    }

    @Override
    public boolean isRegularFile(String path) {
        return Files.isRegularFile(Paths.get(path));
    }

    @Override
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

    @Override
    public List<String> listDirectory2(String path) {
        try (Stream<Path> stream = Files.list(Paths.get(path))) {
            return stream.map(Path::toString).collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public List<String> listRegularFiles(String path, int depth) {
        if (!exists(path)) {
            return null;
        }
        if (depth < 0) {
            return Collections.emptyList();
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
                return paths.stream().filter(this::isRegularFile).collect(Collectors.toList());
            } else {
                return paths.stream().flatMap(s -> listRegularFiles(s, depth - 1).stream())
                        .collect(Collectors.toList());
            }
        }
        // ignore files of other types
        return Collections.emptyList();
    }

    @Override
    public boolean makeDirectory(String path, boolean parents) {
        try {
            if (!parents) {
                Files.createDirectory(Paths.get(path));
            } else {
                Files.createDirectories(Paths.get(path));
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
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
            removeDirectory(PathAccessor.singleton().dirname(path), true);
        }
        return true;
    }

    @Override
    public boolean remove(String path, boolean recursive) {
        if (isRegularFile(path)) {
            try {
                Files.delete(Paths.get(path));
                return true;
            } catch (IOException e) {
                return false;
            }
        } else if (isDirectory(path)) {
            if (recursive) {
                List<String> paths = listDirectory2(path);
                for (String s : paths) {
                    if (!remove(s, true)) {
                        return false;
                    }
                }
            } else {
                return removeDirectory(path, false);
            }
        }
        return false;
    }

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
                if (!copy(PathAccessor.singleton().join(path, basename), PathAccessor.singleton().join(dstPath, basename), true)) {
                    return false;
                }
            }
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
                if (!move(PathAccessor.singleton().join(path, basename), PathAccessor.singleton().join(dstPath, basename))) {
                    return false;
                }
            }
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
        String dirname = PathAccessor.singleton().dirname(path);
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
