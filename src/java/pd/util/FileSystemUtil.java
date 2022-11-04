package pd.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static pd.util.PathUtil.dirname;

class FileSystemUtil {

    public static boolean exists(String path) {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException();
        }
        return Files.exists(Paths.get(path));
    }

    public static boolean mkdir(String path, boolean p) {
        try {
            if (!p) {
                Files.createDirectory(Paths.get(path));
            } else {
                Files.createDirectories(Paths.get(path));
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    public static byte[] load(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    public static String loadText(String path) throws IOException {
        return new String(load(path));
    }

    public static void save(String path, byte[] bytes) throws IOException {
        String dirname = dirname(path);
        if (!exists(dirname)) {
            mkdir(dirname, true);
        }
        Files.write(Paths.get(path), bytes);
    }
}
