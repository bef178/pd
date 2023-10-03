package pd.codec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Digest {

    public static byte[] md5(byte[] bytes) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return md5.digest(bytes);
    }

    public static String md5sum(String s) {
        return HexCodec.toHexString(md5(s.getBytes(StandardCharsets.UTF_8)), false);
    }
}
