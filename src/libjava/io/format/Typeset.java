package libjava.io.format;

import libjava.io.Pullable;
import libjava.io.Pushable;

public class Typeset {

    private static void pushBytes(byte[] a, Pushable pushable) {
        for (int i : a) {
            pushable.push(i & 0xFF);
        }
    }

    public static void pushBytes(Pullable src, int bytesPerLine,
            int startingOffset, byte[] prefix, byte[] suffix,
            Pushable dst) {
        int room = bytesPerLine - startingOffset - suffix.length;
        pushBytes(src.pull(), src, room, dst);

        int last = src.pull();
        while (last != -1) {
            pushBytes(suffix, dst);
            dst.push('\n');
            pushBytes(prefix, dst);

            room = bytesPerLine - prefix.length - suffix.length;
            pushBytes(last, src, room, dst);
        }
    }

    /**
     * @return the size of bytes sent to pusher
     */
    private static void pushBytes(int last, Pullable src, int room, Pushable dst) {
        if (last == -1) {
            return;
        }
        dst.push(last);
        for (int i = 0; i < room; ++i) {
            int j = src.pull();
            if (j == -1) {
                return;
            }
            dst.push(j);
        }
    }
}
