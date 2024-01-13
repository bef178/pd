package pd.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.PrimitiveIterator;

import pd.fenc.ParsingException;

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

    public static PrimitiveIterator.OfInt toIterator(InputStream inputStream) {

        return new PrimitiveIterator.OfInt() {

            private final int NOT_SET = -2;

            private int nextValue = NOT_SET;

            @Override
            public boolean hasNext() {
                if (nextValue == NOT_SET) {
                    try {
                        nextValue = inputStream.read();
                    } catch (IOException e) {
                        throw new ParsingException(e);
                    }
                }
                return nextValue != -1;
            }

            @Override
            public int nextInt() {
                if (nextValue == NOT_SET) {
                    try {
                        nextValue = inputStream.read();
                    } catch (IOException e) {
                        throw new ParsingException(e);
                    }
                }
                if (nextValue == -1) {
                    throw new ParsingException("E: no next");
                }
                int result = nextValue;
                nextValue = NOT_SET;
                return result;
            }
        };
    }

    private InputStreamExtension() {
        // private dummy
    }
}
