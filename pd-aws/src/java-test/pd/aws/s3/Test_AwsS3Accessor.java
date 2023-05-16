package pd.aws.s3;

import java.util.List;
import java.util.Properties;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import pd.util.ResourceExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test_AwsS3Accessor {

    private static final AwsS3Accessor accessor = createAccessor();

    @SneakyThrows
    private static AwsS3Accessor createAccessor() {
        Properties properties = ResourceExtension.resourceAsProperties(".aws.s3.properties");
        String accessKeyId = properties.getProperty("accessKeyId");
        String secretAccessKey = properties.getProperty("secretAccessKey");
        String regionName = properties.getProperty("regionName");
        String endpointUrl = properties.getProperty("endpointUrl");
        String bucketName = properties.getProperty("bucketName");
        return new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
    }

    @Test
    public void test_list2() {
        List<String> paths = accessor.list2("");
        assertNotNull(paths);
        assertTrue(paths.contains("test/"));
        assertTrue(paths.contains("prod/"));
    }
}
