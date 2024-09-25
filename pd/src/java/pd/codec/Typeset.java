package pd.codec;

import pd.fenc.BackableUnicodeProvider;
import pd.fenc.ParsingException;
import pd.fenc.UnicodeConsumer;
import pd.util.AsciiExtension;

public class Typeset {

    private static void appendBytes(byte[] a, UnicodeConsumer dst) {
        for (int i : a) {
            dst.next(checkPrintableAscii(i & 0xFF));
        }
    }

    private static void appendBytes(BackableUnicodeProvider src, int srcSize, UnicodeConsumer dst) {
        for (int i = 0; i < srcSize; i++) {
            if (!src.hasNext()) {
                break;
            }
            dst.next(checkPrintableAscii(src.next()));
        }
    }

    public static void appendBytes(BackableUnicodeProvider src, UnicodeConsumer dst, int numBytesPerLine,
            int startingOffset, byte[] prefix, byte[] suffix) {
        if (prefix == null) {
            prefix = new byte[0];
        }
        if (suffix == null) {
            suffix = new byte[0];
        }
        assert numBytesPerLine > prefix.length + suffix.length;

        int room = numBytesPerLine - startingOffset - suffix.length;
        appendBytes(src, room, dst);

        while (src.hasNext()) {
            appendBytes(suffix, dst);
            dst.next('\n');

            appendBytes(prefix, dst);
            room = numBytesPerLine - prefix.length - suffix.length;
            appendBytes(src, room, dst);
        }
    }

    private static int checkPrintableAscii(int value) {
        if (AsciiExtension.isPrintable(value)) {
            return value;
        }
        throw new ParsingException(
                String.format("Excepted value being SP or visible, actual 0x[%X]", value));
    }
}
