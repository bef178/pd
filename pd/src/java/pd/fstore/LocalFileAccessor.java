package pd.fstore;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import lombok.NonNull;
import pd.util.FileOps;

public class LocalFileAccessor implements FileAccessor {

    @Override
    public List<String> list(@NonNull String prefix) {
        return FileOps.singleton.list(prefix);
    }

    @Override
    public List<String> listAll(@NonNull String prefix) {
        return FileOps.singleton.listAll(prefix);
    }

    @Override
    public boolean removeAll(@NonNull String prefix) {
        for (String path : list(prefix)) {
            if (!FileOps.singleton.removeRecursively(path, null)) {
                return false;
            }
        }
        return true;
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
