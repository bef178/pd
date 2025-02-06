package pd.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_DigestEncoder {

    @Test
    public void test_md5sum() {
        DigestEncoder md5Digest = new DigestEncoder(DigestEncoder.Algorithm.md5);
        assertEquals("60b725f10c9c85c70d97880dfe8191b3", md5Digest.checksum("a\n"));
        assertEquals("60b725f10c9c85c70d97880dfe8191b3", md5Digest.checksum(new ByteArrayInputStream("a\n".getBytes(StandardCharsets.UTF_8))));
        assertEquals("0bee89b07a248e27c83fc3d5951213c1", md5Digest.checksum("abc\n"));
    }

    @Test
    public void test_sha1sum() {
        DigestEncoder sha1Digest = new DigestEncoder(DigestEncoder.Algorithm.sha1);
        assertEquals("3f786850e387550fdab836ed7e6dc881de23001b", sha1Digest.checksum("a\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("3f786850e387550fdab836ed7e6dc881de23001b", sha1Digest.checksum(new ByteArrayInputStream("a\n".getBytes(StandardCharsets.UTF_8))));
        assertEquals("03cfd743661f07975fa2f1220c5194cbaff48451", sha1Digest.checksum("abc\n"));
    }

    @Test
    public void test_sha256sum() {
        DigestEncoder sha256Digest = new DigestEncoder(DigestEncoder.Algorithm.sha256);
        assertEquals("87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7", sha256Digest.checksum("a\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7", sha256Digest.checksum(new ByteArrayInputStream("a\n".getBytes(StandardCharsets.UTF_8))));
        assertEquals("edeaaff3f1774ad2888673770c6d64097e391bc362d7d6fb34982ddf0efd18cb", sha256Digest.checksum("abc\n"));
    }
}
