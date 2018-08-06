package libjava.io.format;

import libjava.io.InstallmentByteBuffer;
import libjava.io.ParsingException;
import libjava.io.Pushable;
import libjava.io.codec.Hexari;

public class RgbText {

    public static int checkRgb(int rgb) {
        if (rgb >= 0 && rgb <= 0xFFFFFF) {
            return rgb;
        }
        throw new ParsingException();
    }

    public static int decode(IntScanner scanner) {
        if (scanner.next() != '#') {
            scanner.back();
            throw new ParsingException();
        }
        int r = Hexari.fromHexariBytes(scanner);
        int g = Hexari.fromHexariBytes(scanner);
        int b = Hexari.fromHexariBytes(scanner);
        return (r << 16) | (g << 8) | b;
    }

    public static int decode(CharSequence rgbText) {
        return decode(IntScanner.wrap(rgbText));
    }

    public static String encode(int rgb) {
        InstallmentByteBuffer buffer = new InstallmentByteBuffer();
        encode(rgb, buffer);
        return new String(buffer.copyBytes());
    }

    public static void encode(int rgb, Pushable pusher) {
        checkRgb(rgb);
        pusher.push('#');
        Hexari.toHexariBytes(getR(rgb), pusher);
        Hexari.toHexariBytes(getG(rgb), pusher);
        Hexari.toHexariBytes(getB(rgb), pusher);
    }

    public static int getB(int rgb) {
        return checkRgb(rgb) & 0xFF;
    }

    public static int getG(int rgb) {
        return (checkRgb(rgb) >>> 8) & 0xFF;
    }

    public static int getR(int rgb) {
        return (checkRgb(rgb) >>> 16) & 0xFF;
    }
}
