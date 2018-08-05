package libjava.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public final class Util {

    /**
     * accept aByte in [0, 0xFF]
     */
    public static int checkByte(int aByte) {
        if (aByte >= 0 && aByte <= 0xFF) {
            return aByte;
        }
        throw new ParsingException();
    }

    /**
     * accept aByte in [0, 0xFF] or -1
     */
    public static int checkByteEx(int aByte) {
        if (aByte >= 0 && aByte <= 0xFF || aByte == -1) {
            return aByte;
        }
        throw new ParsingException();
    }

    public static void closeSilently(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                // dummy
            }
        }
    }

    public static void consumeDataAndCloseSilently(InputStream stream) {
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

    private Util() {
        // dummy
    }
}
