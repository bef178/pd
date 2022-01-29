package pd.fenc;

import static pd.fenc.IReader.EOF;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public final class Util {

    public static int checkAscii(int value) {
        if (Cascii.isAscii(value)) {
            return value;
        }
        throw new ParsingException(
                String.format("Expected value in [0,0xFF], actual [%x]", value));
    }

    /**
     * accept value in range of [0, 0xFF]
     */
    public static int checkByte(int value) {
        if (value >= 0 && value <= 0xFF) {
            return value;
        }
        throw new ParsingException(
                String.format("Expected value in range of [0x00,0xFF], actual 0x[%X]", value));
    }

    /**
     * accept value being -1 or in range of [0, 0xFF]
     */
    public static int checkByteEx(int value) {
        if (value >= 0 && value <= 0xFF || value == -1) {
            return value;
        }
        throw new ParsingException(
                String.format("Expected value in [-1,0xFF], actual [%x]", value));
    }

    public static int checkPrintableAscii(int value) {
        if (Cascii.isPrintable(value)) {
            return value;
        }
        throw new ParsingException(
                String.format("Excepted value being SP or visible, actual 0x[%X]", value));
    }

    public static int checkUnicode(int value) {
        if (value >= 0 && value <= 0x10FFFF) {
            return value;
        }
        throw new ParsingException(
                String.format("Expected value in range of [0x00,0x10FFFF], actual 0x[%X]", value));
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

    public static String codepointToString(int ch) {
        if (ch == EOF) {
            return "EOF";
        }
        return Character.toString(ch);
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
