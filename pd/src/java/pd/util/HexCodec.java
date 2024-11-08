package pd.util;

public class HexCodec {

    public static HexCodec encodeWithLowerCaseLetters() {
        return new HexCodec(false);
    }

    public static HexCodec encodeWithUpperCaseLetters() {
        return new HexCodec(true);
    }

    final int baseLetter;

    public HexCodec() {
        this(true);
    }

    public HexCodec(boolean encodeWithUpperCaseLetters) {
        this.baseLetter = encodeWithUpperCaseLetters ? 'A' : 'a';
    }

    /**
     * consume 1 byte and produce 2 ascii<br/>
     * (byte) 0x61 => ['6','1']
     * (byte) 0x5B => ['5','B']
     */
    public int encode1byte(byte byteValue, int[] dst, int start) {
        dst[start] = (byte) encode4bit((byteValue >> 4) & 0x0F);
        dst[start + 1] = (byte) encode4bit(byteValue & 0x0F);
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

    public String toHexString(byte[] bytes) {
        int[] dst = new int[bytes.length * 2];
        int start = 0;
        for (byte aByte : bytes) {
            start += encode1byte(aByte, dst, start);
        }
        return new String(dst, 0, dst.length);
    }

    /**
     * ['6','1'] => (byte) 0x61
     */
    public byte decode1byte(int hiValue, int loValue) {
        return (byte) ((decode4bit(hiValue) << 4) | decode4bit(loValue));
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
