package pd.aws.s3;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import pd.file.FileAccessor;
import pd.file.FileStat;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
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
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

public class AwsS3Accessor implements FileAccessor {

    private final String bucket;

    private final S3Client s3Client;

    public AwsS3Accessor(String accessKeyId, String secretAccessKey, String region, String endpoint, String bucket) {
        this.bucket = bucket;
        this.s3Client = S3Client.builder()
                .credentialsProvider(() -> AwsBasicCredentials.create(accessKeyId, secretAccessKey))
                .region(Region.of(region))
                .endpointOverride(endpoint == null ? null : URI.create(endpoint))
                .build();
    }

    @Override
    public List<String> list(String keyPrefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(keyPrefix)
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
    public List<String> listAll(String keyPrefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(keyPrefix)
                .build();
        return s3Client.listObjectsV2Paginator(request).stream()
                .flatMap(a -> a.contents().stream())
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    private List<S3Object> listS3Objects(String keyPrefix, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(keyPrefix)
                .maxKeys(limit)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents();
    }

    @Override
    public FileStat stat(String key) {
        HeadObjectRequest request = HeadObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        HeadObjectResponse response = s3Client.headObject(request);
        FileStat fileStat = new FileStat();
        fileStat.key = key;
        fileStat.contentLength = response.contentLength();
        fileStat.lastModified = request.ifModifiedSince().toEpochMilli();
        return fileStat;
    }

    @Override
    public boolean remove(String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        DeleteObjectResponse response = s3Client.deleteObject(request);
        return response.sdkHttpResponse().isSuccessful();
    }

    public boolean remove(Collection<String> keys) {
        List<ObjectIdentifier> objectIdentifiers = keys.stream()
                .map(a -> ObjectIdentifier.builder().key(a).build())
                .collect(Collectors.toList());
        return removeObjectIdentifiers(objectIdentifiers);
    }

    @Override
    public boolean removeAll(String keyPrefix) {
        while (true) {
            List<S3Object> s3Objects = listS3Objects(keyPrefix, 1000);
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
    public byte[] load(String key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    public boolean save(String key, byte[] bytes) {
        return save(key, RequestBody.fromBytes(bytes));
    }

    private boolean save(String key, RequestBody requestBody) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
        PutObjectResponse response = s3Client.putObject(request, requestBody);
        return response.sdkHttpResponse().isSuccessful();
    }

    public boolean uploadTo(String key, String localPath) {
        return save(key, RequestBody.fromFile(new File(localPath)));
    }
}
