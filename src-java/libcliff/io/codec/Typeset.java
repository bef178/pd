package libcliff.io.codec;

import libcliff.io.Pullable;
import libcliff.io.Pushable;

public class Typeset {

    private static int pushBytes(byte[] a, Pushable pushable) {
        int size = 0;
        for (int i : a) {
            size += pushable.push(i & 0xFF);
        }
        return size;
    }

    public static int pushBytes(Pullable src, int bytesPerLine,
            int startingOffset, byte[] prefix, byte[] suffix,
            Pushable pushable) {
        int rest = bytesPerLine - startingOffset - suffix.length;
        int size = pushBytes(src.pull(), src, rest, pushable);

        int pulled = src.pull();
        while (pulled != -1) {
            size += pushBytes(suffix, pushable);
            size += pushable.push('\n');
            size += pushBytes(prefix, pushable);

            rest = bytesPerLine - prefix.length - suffix.length;
            size += pushBytes(pulled, src, rest, pushable);
        }
        return size;
    }

    /**
     * @return the size of bytes sent to pusher
     */
    private static int pushBytes(int prepulled, Pullable src, int rest,
            Pushable pushable) {
        if (prepulled == -1) {
            return 0;
        }
        int size = pushable.push(prepulled);
        for (int i = 0; i < rest; ++i) {
            int j = src.pull();
            if (j == -1) {
                return size;
            }
            size += pushable.push(j);
        }
        return size;
    }
}
