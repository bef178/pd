package pd.fenc;

public class HexCodec {

    /**
     * ['9','7'] => 'a'
     *
     * always consume 2 bytes
     */
    public static int decode1byte(byte[] src, int i) {
        int dstByte = decode4bit(src[i++]);
        return (dstByte << 4) | decode4bit(src[i]);
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
     * 'a' => ['9','7']
     *
     * always produce 2 bytes
     */
    public static void encode1byte(int srcByte, byte[] dst, int start) {
        assert (srcByte >= 0 && srcByte <= 0xFF);
        dst[start++] = (byte) encode4bit((srcByte >> 4) & 0x0F);
        dst[start] = (byte) encode4bit(srcByte & 0x0F);
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
