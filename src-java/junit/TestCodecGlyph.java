package junit;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import cc.typedef.adt.Blob;
import cc.typedef.io.Codec;
import cc.typedef.io.InstallmentByteBuffer;
import cc.typedef.io.Nextable;

public class TestCodecGlyph {

    @Test
    public void testBasicMultilingualPlane() {
        // 汉 27721 =utf8=E6B189
        int expectedCodePoint = "汉".codePointAt(0);
        String expected = "\\uE6B189";

        Blob blob = new Blob(8);
        int n = Codec.Glyph.encode(expectedCodePoint, blob);
        Assert.assertTrue(Arrays.equals(expected.getBytes(), blob.a));
        Assert.assertEquals(8, n);

        Nextable it = new InstallmentByteBuffer().append(expected)
                .reader();
        int ch = Codec.Glyph.decode(it);
        Assert.assertEquals(expectedCodePoint, ch);
    }

    @Test
    public void testEscape() {
        Nextable it = new InstallmentByteBuffer().append("\\n").reader();
        int ch = Codec.Glyph.decode(it);
        Assert.assertEquals('\n', ch);
    }
}
