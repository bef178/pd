package pd.codec;

import pd.util.AsciiExtension;

public class HexCodec {

    /**
     * ['6','1'] => (byte) 0x61
     */
    public static byte decode1byte(int hiValue, int loValue) {
        int byteValue = (decode4bit(hiValue) << 4) | decode4bit(loValue);
        return (byte) byteValue;
    }

    private static int decode4bit(int ch) {
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

    /**
     * consume 1 byte and produce 2 ascii using upper case letter<br/>
     * (byte) 0x61 => ['6','1']
     * (byte) 0x5B => ['5','B']
     */
    public static void encode1byte(byte byteValue, int[] dst, int start) {
        int out1st = (byte) encode4bit((byteValue >> 4) & 0x0F);
        int out2nd = (byte) encode4bit(byteValue & 0x0F);
        dst[start] = out1st;
        dst[start + 1] = out2nd;
    }

    public static StringBuilder encode1byte(byte byteValue, StringBuilder sb, boolean useUpperCase) {
        int out1st = encode4BitHi(byteValue);
        int out2nd = encode4BitLo(byteValue);
        if (!useUpperCase) {
            out1st = AsciiExtension.toLower(out1st);
            out2nd = AsciiExtension.toLower(out2nd);
        }
        return sb.appendCodePoint(out1st).appendCodePoint(out2nd);
    }

    /**
     * return [0-9A-F]
     */
    public static int encode4BitHi(int byteValue) {
        return encode4bit((byteValue >> 4) & 0x0F);
    }

    /**
     * return [0-9A-F]
     */
    public static int encode4BitLo(int byteValue) {
        return encode4bit(byteValue & 0x0F);
    }

    /**
     * return [0-9A-F]
     */
    private static int encode4bit(int value) {
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
                return value - 10 + 'A';
            default:
                break;
        }
        throw new IllegalArgumentException();
    }

    public static String toHexString(byte[] bytes, boolean userUpperCase) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            encode1byte(aByte, sb, userUpperCase);
        }
        return sb.toString();
    }
}
