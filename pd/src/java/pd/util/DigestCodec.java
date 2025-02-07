package pd.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import lombok.SneakyThrows;

/**
 * 摘要算法会损失信息量：名为codec实为encoder<br/>
 * 对象化；static方法仅用于封装构造函数；类本身不作为singleton管理器<br/>
 */
public class DigestCodec {

    public static DigestCodec md5() {
        return new DigestCodec("md5");
    }

    public static DigestCodec sha1() {
        return new DigestCodec("sha1");
    }

    public static DigestCodec sha256() {
        return new DigestCodec("sha256");
    }

    public static DigestCodec sha512() {
        return new DigestCodec("sha512");
    }

    private final HexCodec hexCodec = HexCodec.withLowerCaseLetters();

    private final String algoName;

    private DigestCodec(String algoName) {
        this.algoName = algoName;
    }

    @SneakyThrows
    public byte[] digest(byte[] src) {
        return digest(new ByteArrayInputStream(src));
    }

    @SneakyThrows
    public byte[] digest(InputStream inputStream) {
        MessageDigest md = MessageDigest.getInstance(algoName);
        byte[] buffer = new byte[512 * 1024];
        int nRead;
        while ((nRead = inputStream.read(buffer)) > 0) {
            md.update(buffer, 0, nRead);
        }
        return md.digest();
    }

    public String checksum(byte[] bytes) {
        return hexCodec.encodeToString(digest(bytes));
    }

    public String checksum(InputStream inputStream) {
        return hexCodec.encodeToString(digest(inputStream));
    }

    public String checksum(String s) {
        return hexCodec.encodeToString(digest(s.getBytes(StandardCharsets.UTF_8)));
    }
}
