package cc.typedef.io;

import cc.typedef.adt.Blob;

class CodecGlyph {

    public static int decode(Nextable it) {
        assert it != null;
        assert it.hasNext();
        int last = it.next();
        return decode(last, it);
    }

    public static int decode(int last, Nextable it) {
        expect('\\', last);
        assert it.hasNext();
        int ch = it.next();
        switch (ch) {
            case '\\':
                return '\\';
            case 'a':
                return 0x07;
            case 't':
                return 0x09;
            case 'n':
                return 0x0A;
            case 'v':
                return 0x0B;
            case 'f':
                return 0x0C;
            case 'r':
                return 0x0D;
            case 'b':
                return '\b';
            case 'u':
                return Codec.Unicode.fromUtf8HexBytes(it);
            default:
                return ch;
        }
    }

    public static int encode(int ch, Blob blob) {
        int n = 2 + Codec.Unicode.utf8Length(ch) * 2;
        if (blob != null) {
            if (blob.isEmpty()) {
                blob.init(n);
            }
            int start = blob.i;
            blob.next((byte) '\\');
            blob.next((byte) 'u');
            int m = Codec.Unicode.toUtf8HexBytes(ch, blob);
            assert m == n - 2;
            blob.i = start;
        }
        return n;
    }

    private static void expect(int expected, int actual) {
        if (expected != actual) {
            throw new IllegalArgumentException("Excepts \'"
                    + (char) expected + "\', gets \'" + (char) actual
                    + "\'");
        }
    }
}
