package pd.codec;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_Md5Digest {

    @Test
    public void test_md5sum() {
        assertEquals("60b725f10c9c85c70d97880dfe8191b3", Md5Digest.md5sum("a\n"));
        assertEquals("0bee89b07a248e27c83fc3d5951213c1", Md5Digest.md5sum("abc\n"));
    }
}
