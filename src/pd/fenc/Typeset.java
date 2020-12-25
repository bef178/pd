package pd.fenc;

import static pd.fenc.Util.checkSingleWidthAscii;

public class Typeset {

    private static void appendBytes(byte[] a, IWriter dst) {
        for (int i : a) {
            dst.append(checkSingleWidthAscii(i & 0xFF));
        }
    }

    private static void appendBytes(IReader src, int srcSize, IWriter dst) {
        for (int i = 0; i < srcSize; i++) {
            if (!src.hasNext()) {
                break;
            }
            dst.append(checkSingleWidthAscii(src.next()));
        }
    }

    public static void appendBytes(IReader src, IWriter dst, int numBytesPerLine,
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
            dst.append('\n');

            appendBytes(prefix, dst);
            room = numBytesPerLine - prefix.length - suffix.length;
            appendBytes(src, room, dst);
        }
    }
}
