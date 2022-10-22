package pd.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class InputStreamExtension {

    public static byte[] readAllBytes(InputStream stream) throws IOException {
        final int CHUNK_SIZE = 4096;
        List<byte[]> buffers = new LinkedList<>();
        int size = 0;
        while (true) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int nRead = stream.read(buffer);
            if (nRead == -1) {
                break;
            }
            buffers.add(buffer);
            size += nRead;
        }
        byte[] result = new byte[size];
        int start = 0;
        for (byte[] buffer : buffers) {
            if (start + CHUNK_SIZE < size) {
                System.arraycopy(buffer, 0, result, start, CHUNK_SIZE);
                start += CHUNK_SIZE;
            } else {
                System.arraycopy(buffer, 0, result, start, size - start);
                start = size;
            }
        }
        return result;
    }

    private InputStreamExtension() {
        // private dummy
    }
}
