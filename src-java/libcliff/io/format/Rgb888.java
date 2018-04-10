package libcliff.io.format;

import libcliff.io.ParsingException;
import libcliff.io.Pullable;
import libcliff.io.Pushable;
import libcliff.io.codec.Hexari;

public class Rgb888 {

    public static int checkRgb888(int color) {
        if (color >= 0 && color <= 0xFFFFFF) {
            return color;
        }
        throw new ParsingException();
    }

    public static int decode(int prefix, Pullable pullable) {
        if (prefix != '#') {
            throw new ParsingException('#', prefix);
        }
        int r = pullComponent(pullable);
        int g = pullComponent(pullable);
        int b = pullComponent(pullable);
        return (r << 16) | (g << 8) | b;
    }

    public static int encode(int color, Pushable pushable) {
        checkRgb888(color);
        pushable.push('#');
        Hexari.toHexariBytes(getR(color), pushable);
        Hexari.toHexariBytes(getG(color), pushable);
        Hexari.toHexariBytes(getB(color), pushable);
        return 1;
    }

    public static int getB(int color) {
        return checkRgb888(color) & 0xFF;
    }

    public static int getG(int color) {
        return (checkRgb888(color) >>> 8) & 0xFF;
    }

    public static int getR(int color) {
        return (checkRgb888(color) >>> 16) & 0xFF;
    }

    private static int pullComponent(Pullable pullable) {
        int i = pullable.pull();
        int j = pullable.pull();
        return Hexari.fromHexariBytes(i, j);
    }
}
