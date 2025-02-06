package pd.util;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Digest {

    private final HexCodec hexCodec = HexCodec.withLowerCaseLetters();

    public byte[] md5(byte[] bytes) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return md5.digest(bytes);
    }

    public String md5sum(byte[] bytes) {
        return hexCodec.encodeToString(md5(bytes));
    }

    public String md5sum(String s) {
        return hexCodec.encodeToString(md5(s.getBytes(StandardCharsets.UTF_8)));
    }

    public String md5sum(InputStream inputStream) {
        MessageDigest md;
        try {
            int nRead;
            byte[] buffer = new byte[512 * 1024];
            md = MessageDigest.getInstance("md5");
            while ((nRead = inputStream.read(buffer)) > 0) {
                md.update(buffer, 0, nRead);
            }
            return hexCodec.encodeToString(md5(md.digest()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
