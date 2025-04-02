package pd.util;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import lombok.SneakyThrows;

public class ZipExtension {

    @SneakyThrows
    public static byte[] readZipEntry(String pathToZip, String entryName) {
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(pathToZip)))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String name = zipEntry.getName();
                if (entryName.equals(name)) {
                    byte[] bytes = InputStreamExtension.readAllBytes(zipInputStream);
                    zipInputStream.closeEntry();
                    return bytes;
                } else {
                    zipInputStream.closeEntry();
                }
            }
        }
        return null;
    }

    @SneakyThrows
    public static boolean unzip(String pathToZip, String pathToParityDirectory) {
        File dstRoot = new File(pathToParityDirectory);
        if (!dstRoot.exists()) {
            if (!dstRoot.mkdirs()) {
                return false;
            }
        }
        byte[] buffer = new byte[512 * 1024];
        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(Paths.get(pathToZip)))) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String zipEntryName = zipEntry.getName();
                File dstFile = new File(PathExtension.join(pathToParityDirectory, zipEntryName));
                if (zipEntry.isDirectory()) {
                    if (!dstFile.mkdir()) {
                        return false;
                    }
                } else {
                    try (FileOutputStream outputStream = new FileOutputStream(dstFile)) {
                        int nRead;
                        while ((nRead = zipInputStream.read(buffer)) >= 0) {
                            if (nRead > 0) {
                                outputStream.write(buffer, 0, nRead);
                            }
                        }
                    }
                }
                zipInputStream.closeEntry();
            }
        }
        return true;
    }
}
