package junit;

import java.nio.charset.Charset;
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

        Nextable it = new InstallmentByteBuffer().append(expected).reader();
        int ch = Codec.Glyph.decode(it);
        Assert.assertEquals(expectedCodePoint, ch);
    }

    @Test
    public void testEscape1() {
        Nextable it = new InstallmentByteBuffer().append("\\n").reader();
        int ch = Codec.Glyph.decode(it);
        Assert.assertEquals('\n', ch);
    }

    @Test
    public void testEscape2() {
        Nextable it = new Blob("\\u20".getBytes(), 0);
        int slash = it.next();
        int actual = Codec.Glyph.decode(slash, it);
        Assert.assertEquals(' ', actual);
    }

    @Test
    public void testUtf8() {
        // 冬: 0x02F81A 194586
        // 你: 0x4F60 20320
        // 我: 0x6211 25105
        // 他: 0x4ED6 20182
        final String[] a = {
                "a", "\\", "$", "冬", "你", "我", "他"
        };

        for (String s : a) {
            int expected = s.codePointAt(0);
            Blob blob = new Blob(s.getBytes(Charset.forName("UTF-8")), 0);
            Assert.assertEquals(expected,
                    Codec.Glyph.fromUtf8Bytes(blob, false));
        }

        for (String s : a) {
            byte[] expected = s.getBytes(Charset.forName("UTF-8"));
            int expectedCodePoint = s.codePointAt(0);
            Blob blob = new Blob(8);
            int n = Codec.Glyph.toUtf8Bytes(expectedCodePoint, blob, false);
            for (int i = 0; i < n; ++i) {
                Assert.assertEquals(expected[i], blob.a[i]);
            }
        }
    }
}
