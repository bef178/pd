package pd.codec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Codec {

    public static String md5sum(byte[] bytes) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] digest = md5.digest(bytes);
        return HexCodec.toHexString(digest, false);
    }
}
