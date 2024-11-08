package pd.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_Md5Digest {

    final Md5Digest md5Digest = new Md5Digest();

    @Test
    public void test_md5sum() {
        Assertions.assertEquals("60b725f10c9c85c70d97880dfe8191b3", md5Digest.md5sum("a\n"));
        assertEquals("0bee89b07a248e27c83fc3d5951213c1", md5Digest.md5sum("abc\n"));
    }
}
