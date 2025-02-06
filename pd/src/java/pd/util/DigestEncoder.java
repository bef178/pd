package pd.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import lombok.SneakyThrows;

public class DigestEncoder {

    public static DigestEncoder md5() {
        return new DigestEncoder("md5");
    }

    public static DigestEncoder sha1() {
        return new DigestEncoder("sha1");
    }

    public static DigestEncoder sha256() {
        return new DigestEncoder("sha256");
    }

    public static DigestEncoder sha512() {
        return new DigestEncoder("sha512");
    }

    private final HexCodec hexCodec = HexCodec.withLowerCaseLetters();

    private final String algoName;

    private DigestEncoder(String algoName) {
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
