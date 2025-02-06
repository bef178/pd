package pd.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_DigestEncoder {

    @Test
    public void test_md5sum() {
        DigestEncoder md5Digest = DigestEncoder.md5();
        assertEquals("60b725f10c9c85c70d97880dfe8191b3", md5Digest.checksum("a\n"));
        assertEquals("60b725f10c9c85c70d97880dfe8191b3", md5Digest.checksum(new ByteArrayInputStream("a\n".getBytes(StandardCharsets.UTF_8))));
        assertEquals("0bee89b07a248e27c83fc3d5951213c1", md5Digest.checksum("abc\n"));
    }

    @Test
    public void test_sha1sum() {
        DigestEncoder sha1Digest = DigestEncoder.sha1();
        assertEquals("3f786850e387550fdab836ed7e6dc881de23001b", sha1Digest.checksum("a\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("3f786850e387550fdab836ed7e6dc881de23001b", sha1Digest.checksum(new ByteArrayInputStream("a\n".getBytes(StandardCharsets.UTF_8))));
        assertEquals("03cfd743661f07975fa2f1220c5194cbaff48451", sha1Digest.checksum("abc\n"));
    }

    @Test
    public void test_sha256sum() {
        DigestEncoder sha256Digest = DigestEncoder.sha256();
        assertEquals("87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7", sha256Digest.checksum("a\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("87428fc522803d31065e7bce3cf03fe475096631e5e07bbd7a0fde60c4cf25c7", sha256Digest.checksum(new ByteArrayInputStream("a\n".getBytes(StandardCharsets.UTF_8))));
        assertEquals("edeaaff3f1774ad2888673770c6d64097e391bc362d7d6fb34982ddf0efd18cb", sha256Digest.checksum("abc\n"));
    }

    @Test
    public void test_sha512sum() {
        DigestEncoder sha512Digest = DigestEncoder.sha512();
        assertEquals("162b0b32f02482d5aca0a7c93dd03ceac3acd7e410a5f18f3fb990fc958ae0df6f32233b91831eaf99ca581a8c4ddf9c8ba315ac482db6d4ea01cc7884a635be", sha512Digest.checksum("a\n".getBytes(StandardCharsets.UTF_8)));
        assertEquals("4f285d0c0cc77286d8731798b7aae2639e28270d4166f40d769cbbdca5230714d848483d364e2f39fe6cb9083c15229b39a33615ebc6d57605f7c43f6906739d", sha512Digest.checksum("abc\n"));
    }
}
