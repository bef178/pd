package pd.util;

public class HexCodec {

    public static HexCodec withLowerCaseLetters() {
        return new HexCodec(false);
    }

    public static HexCodec withUpperCaseLetters() {
        return new HexCodec(true);
    }

    final int baseLetter;

    private HexCodec(boolean withUpperCaseLetters) {
        this.baseLetter = withUpperCaseLetters ? 'A' : 'a';
    }

    /**
     * (byte) 0x61 => ['6','1']
     */
    public int encode(byte octet, int[] dst, int start) {
        dst[start] = encode4bit((octet >> 4) & 0x0F);
        dst[start + 1] = encode4bit(octet & 0x0F);
        return 2;
    }

    /**
     * accept [0,16)
     * return [0-9A-F]
     */
    private int encode4bit(int value) {
        switch (value) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
                return value + '0';
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                return value - 10 + baseLetter;
            default:
                break;
        }
        throw new IllegalArgumentException();
    }

    public int[] encodeToArray(byte... octets) {
        int[] dst = new int[octets.length * 2];
        int start = 0;
        for (byte octet : octets) {
            start += encode(octet, dst, start);
        }
        return dst;
    }

    public String encodeToString(byte... octets) {
        int[] a = encodeToArray(octets);
        return new String(a, 0, a.length);
    }

    /**
     * ['6','1'] => (byte) 0x61
     */
    public byte decode(int a0, int a1) {
        return (byte) ((decode4bit(a0) << 4) | decode4bit(a1));
    }

    private int decode4bit(int ch) {
        switch (ch) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return ch - '0';
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
                return ch - 'A' + 10;
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
                return ch - 'a' + 10;
            default:
                break;
        }
        throw new IllegalArgumentException();
    }
}
