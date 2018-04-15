package libcliff.io.format;

import libcliff.io.InstallmentByteBuffer;
import libcliff.io.ParsingException;
import libcliff.io.Pullable;
import libcliff.io.Pushable;
import libcliff.io.codec.Hexari;

public class RgbText {

    public static int checkRgb(int rgb) {
        if (rgb >= 0 && rgb <= 0xFFFFFF) {
            return rgb;
        }
        throw new ParsingException();
    }

    public static int decode(Feeder puller) {
        if (puller.next() != '#') {
            puller.back();
            throw new ParsingException();
        }
        int r = pullComponent(puller);
        int g = pullComponent(puller);
        int b = pullComponent(puller);
        return (r << 16) | (g << 8) | b;
    }

    public static int decode(String rgbText) {
        return decode(Feeder.wrap(rgbText));
    }

    public static String encode(int rgb) {
        InstallmentByteBuffer pusher = new InstallmentByteBuffer();
        encode(rgb, pusher);
        return new String(pusher.copyBytes());
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

    private static int pullComponent(Pullable pullable) {
        int i = pullable.pull();
        int j = pullable.pull();
        return Hexari.fromHexariBytes(i, j);
    }
}
