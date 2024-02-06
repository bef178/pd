package pd.util;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class InputStreamExtension {

    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int CHUNK_SIZE = 4096;
        List<byte[]> buffers = new LinkedList<>();
        int size = 0;
        while (true) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int nRead = inputStream.read(buffer);
            if (nRead == -1) {
                break;
            }
            if (nRead == CHUNK_SIZE) {
                buffers.add(buffer);
            } else {
                buffers.add(Arrays.copyOfRange(buffer, 0, nRead));
            }
            size += nRead;
        }
        byte[] result = new byte[size];
        int start = 0;
        for (byte[] buffer : buffers) {
            System.arraycopy(buffer, 0, result, start, buffer.length);
            start += buffer.length;
        }
        return result;
    }

    public static void save(InputStream inputStream, String dstPath) throws IOException {
        final int CHUNK_SIZE = 4096;
        try (FileOutputStream outputStream = new FileOutputStream(dstPath, false)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            while (true) {
                int nRead = inputStream.read(buffer);
                if (nRead == -1) {
                    break;
                }
                outputStream.write(buffer, 0, nRead);
            }
        }
    }

    public static void consumeAndCloseSilently(InputStream stream) {
        final int BUFFER_SIZE = 4096;
        final byte[] bytes = new byte[BUFFER_SIZE];
        try {
            while (stream.read(bytes, 0, BUFFER_SIZE) != -1) {
                // dummy
            }
        } catch (IOException e) {
            // dummy
        } finally {
            closeSilently(stream);
        }
    }

    private static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                // dummy
            }
        }
    }

    private InputStreamExtension() {
        // private dummy
    }
}
