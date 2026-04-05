package pd.aws.s3;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import pd.fstore.FileAccessor;
import pd.fstore.FileStat;
import pd.util.InputStreamExtension;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CommonPrefix;
import software.amazon.awssdk.services.s3.model.Delete;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectsResponse;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AwsS3Accessor implements FileAccessor {

    private final S3Client s3Client;

    private final String bucket;

    public AwsS3Accessor(String accessKeyId, String secretAccessKey, String region, String endpoint, String bucket) {
        this.s3Client = S3Client.builder()
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, secretAccessKey))
                .region(Region.of(region))
                .endpointOverride(endpoint == null ? null : URI.create(endpoint))
                .build();
        this.bucket = bucket;
    }

    @Override
    public List<String> list(@NonNull String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .delimiter("/")
                .maxKeys(1000)
                .build();

        List<CommonPrefix> commonPrefixes = new LinkedList<>();
        List<S3Object> s3Objects = new LinkedList<>();

        // XXX use Paginator?
        boolean isTruncated = true;
        while (isTruncated) {
            ListObjectsV2Response response = s3Client.listObjectsV2(request);
            if (response.hasCommonPrefixes()) {
                commonPrefixes.addAll(response.commonPrefixes());
            }
            if (response.hasContents()) {
                s3Objects.addAll(response.contents());
            }
            isTruncated = response.isTruncated();
            request = request.toBuilder()
                    .continuationToken(response.nextContinuationToken())
                    .build();
        }

        List<String> a = new ArrayList<>(commonPrefixes.size() + s3Objects.size());
        a.addAll(commonPrefixes.stream().map(CommonPrefix::prefix).collect(Collectors.toList()));
        a.addAll(s3Objects.stream().map(S3Object::key).collect(Collectors.toList()));
        return a;
    }

    @Override
    public List<String> listAll(@NonNull String prefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .build();
        return s3Client.listObjectsV2Paginator(request).stream()
                .flatMap(a -> a.contents().stream())
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    private List<S3Object> listS3Objects(String prefix, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(prefix)
                .maxKeys(limit)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents();
    }

    @Override
    public boolean removeAll(@NonNull String prefix) {
        while (true) {
            List<S3Object> s3Objects = listS3Objects(prefix, 1000);
            if (s3Objects == null || s3Objects.isEmpty()) {
                break;
            }
            List<ObjectIdentifier> objectIdentifiers = s3Objects.stream()
                    .map(a -> ObjectIdentifier.builder().key(a.key()).build())
                    .collect(Collectors.toList());
            if (!removeObjectIdentifiers(objectIdentifiers)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public FileStat stat(@NonNull String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        HeadObjectResponse response = s3Client.headObject(request);
        FileStat fileStat = new FileStat();
        fileStat.key = key;
        fileStat.contentLength = response.contentLength();
        fileStat.lastModified = response.lastModified().toEpochMilli();
        return fileStat;
    }

    @Override
    public boolean remove(@NonNull String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        DeleteObjectResponse response = s3Client.deleteObject(request);
        return response.sdkHttpResponse().isSuccessful();
    }

    public boolean remove(@NonNull Collection<String> keys) {
        List<ObjectIdentifier> objectIdentifiers = keys.stream()
                .map(a -> ObjectIdentifier.builder().key(a).build())
                .collect(Collectors.toList());
        return removeObjectIdentifiers(objectIdentifiers);
    }

    private boolean removeObjectIdentifiers(Collection<ObjectIdentifier> objectIdentifiers) {
        DeleteObjectsRequest request = DeleteObjectsRequest.builder()
                .bucket(bucket)
                .delete(Delete.builder()
                        .objects(objectIdentifiers)
                        .build())
                .build();
        DeleteObjectsResponse response = s3Client.deleteObjects(request);
        return response.sdkHttpResponse().isSuccessful();
    }

    @Override
    public byte[] load(@NonNull String key) {
        return loadObject(key).asByteArray();
    }

    private ResponseBytes<GetObjectResponse> loadObject(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(request);
    }

    public void download(@NonNull String key, String localParity) throws IOException {
        try (InputStream inputStream = loadObject(key).asInputStream()) {
            new File(localParity).getParentFile().mkdirs();
            InputStreamExtension.save(inputStream, localParity);
        }
    }

    @Override
    public boolean save(@NonNull String key, byte[] bytes) {
        return save(key, RequestBody.fromBytes(bytes));
    }

    private boolean save(@NonNull String key, RequestBody requestBody) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        PutObjectResponse response = s3Client.putObject(request, requestBody);
        return response.sdkHttpResponse().isSuccessful();
    }

    public boolean upload(@NonNull String key, String localParity) {
        return save(key, RequestBody.fromFile(new File(localParity)));
    }
}
