package pd.codec;

import pd.fenc.Int32Feeder;
import pd.fenc.Int32Pusher;
import pd.fenc.ParsingException;
import pd.util.AsciiExtension;

public class Typeset {

    private static void appendBytes(byte[] a, Int32Pusher dst) {
        for (int i : a) {
            dst.push(checkPrintableAscii(i & 0xFF));
        }
    }

    private static void appendBytes(Int32Feeder src, int srcSize, Int32Pusher dst) {
        for (int i = 0; i < srcSize; i++) {
            if (!src.hasNext()) {
                break;
            }
            dst.push(checkPrintableAscii(src.next()));
        }
    }

    public static void appendBytes(Int32Feeder src, Int32Pusher dst, int numBytesPerLine,
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
            dst.push('\n');

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
