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
        // 我 25105 =utf8=E68891
        int expectedGlyph = "我".codePointAt(0);
        byte[] expectedBytes = "\\uE68891".getBytes();

        Blob blob = new Blob(8);
        int n = Codec.Glyph.encode(expectedGlyph, blob);
        Assert.assertTrue(Arrays.equals(expectedBytes, blob.a));
        Assert.assertEquals(8, n);

        Nextable it = new InstallmentByteBuffer().append(expectedBytes)
                .reader();
        int ch = Codec.Glyph.decode(it);
        Assert.assertEquals(expectedGlyph, ch);
    }

    @Test
    public void testEscape() {
        Nextable it = new InstallmentByteBuffer().append("\\n").reader();
        int ch = Codec.Glyph.decode(it);
        Assert.assertEquals('\n', ch);
    }
}
