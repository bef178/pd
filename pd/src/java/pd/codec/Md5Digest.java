package pd.codec;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import pd.util.HexCodec;

public class Md5Digest {

    private static final HexCodec hexCodec = HexCodec.encodeWithLowerCaseLetters();

    public static byte[] md5(byte[] bytes) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return md5.digest(bytes);
    }

    public static String md5sum(byte[] bytes) {
        return hexCodec.toHexString(md5(bytes));
    }

    public static String md5sum(String s) {
        return hexCodec.toHexString(md5(s.getBytes(StandardCharsets.UTF_8)));
    }

    public static String md5sum(InputStream inputStream) {
        MessageDigest md;
        try {
            int nRead;
            byte[] buffer = new byte[512 * 1024];
            md = MessageDigest.getInstance("md5");
            while ((nRead = inputStream.read(buffer)) > 0) {
                md.update(buffer, 0, nRead);
            }
            return hexCodec.toHexString(md5(md.digest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
