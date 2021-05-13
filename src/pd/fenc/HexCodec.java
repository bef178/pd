package pd.fenc;

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
     * always consume 1 byte and produce 2 int32<br/>
     * 0x61 => ['6','1']
     */
    public static void encode1byte(byte byteValue, int[] dst, int start) {
        dst[start] = (byte) encode4bit((byteValue >> 4) & 0x0F);
        dst[start + 1] = (byte) encode4bit(byteValue & 0x0F);
    }

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
                return value + 'A';
            default:
                break;
        }
        throw new IllegalArgumentException();
    }
}
