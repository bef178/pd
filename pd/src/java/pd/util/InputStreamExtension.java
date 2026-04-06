package pd.util;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;

import pd.fenc.ParsingException;

import static pd.util.AsciiExtension.EOF;

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

            private final int NO_VALUE = -9;

            private int nextValue = NO_VALUE;

            @Override
            public boolean hasNext() {
                if (nextValue == NO_VALUE) {
                    try {
                        nextValue = inputStream.read();
                    } catch (IOException e) {
                        throw new ParsingException(e);
                    }
                }
                return nextValue != EOF;
            }

            @Override
            public int nextInt() {
                if (nextValue == NO_VALUE) {
                    try {
                        nextValue = inputStream.read();
                    } catch (IOException e) {
                        throw new ParsingException(e);
                    }
                }
                if (nextValue == EOF) {
                    throw new NoSuchElementException();
                }
                int result = nextValue;
                nextValue = NO_VALUE;
                return result;
            }
        };
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
