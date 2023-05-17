package pd.aws.s3;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import pd.file.FileAccessor;
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
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.ObjectIdentifier;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;
import software.amazon.awssdk.services.s3.paginators.ListObjectsV2Iterable;

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
    public boolean isRegularFile(String path) {
        List<S3Object> s3Objects = listS3Objects(path, 1);
        return s3Objects != null && s3Objects.get(0) != null;
    }

    public List<S3Object> listS3Objects(String pathPrefix, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }

        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(pathPrefix)
                .maxKeys(limit)
                .build();
        ListObjectsV2Response response = s3Client.listObjectsV2(request);
        return response.contents();
    }

    @Override
    public List<String> list2(String pathPrefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(pathPrefix)
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
    public List<String> listAllRegularFiles(String pathPrefix) {
        return listAllS3Objects(pathPrefix).stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    private List<S3Object> listAllS3Objects(String pathPrefix) {
        ListObjectsV2Request request = ListObjectsV2Request.builder()
                .bucket(bucket)
                .prefix(pathPrefix)
                .build();

        ListObjectsV2Iterable itRequest = s3Client.listObjectsV2Paginator(request);
        return itRequest.stream()
                .flatMap(a -> a.contents().stream())
                .collect(Collectors.toList());
    }

    @Override
    public boolean makeDirectory(String path, boolean parents) {
        return true;
    }

    @Override
    public boolean removeDirectory(String path, boolean parents) {
        return true;
    }

    @Override
    public boolean remove(String path, boolean recursive) {
        if (path == null) {
            throw new IllegalArgumentException("path should not be null");
        }
        if (!path.endsWith("/")) {
            if (recursive) {
                throw new IllegalArgumentException("do you mean remove regular file recursively?");
            }
            return removeRegularFile(path);
        } else {
            if (recursive) {
                while (true) {
                    List<S3Object> s3Objects = listS3Objects(path, 1000);
                    if (s3Objects == null || s3Objects.isEmpty()) {
                        break;
                    }
                    removeS3Objects(s3Objects);
                }
                return true;
            } else {
                return removeDirectory(path, false);
            }
        }
    }

    public boolean removeRegularFile(String path) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();
        DeleteObjectResponse response = s3Client.deleteObject(request);
        return response.sdkHttpResponse().isSuccessful();
    }

    public boolean removeRegularFiles(String... paths) {
        if (paths.length == 1) {
            return removeRegularFile(paths[0]);
        }
        return removeRegularFiles(Arrays.asList(paths));
    }

    public boolean removeRegularFiles(Collection<String> paths) {
        if (paths.size() == 1) {
            return removeRegularFile(paths.iterator().next());
        }

        List<ObjectIdentifier> objectIdentifiers = paths.stream()
                .map(a -> ObjectIdentifier.builder().key(a).build())
                .collect(Collectors.toList());
        return removeObjectIdentifiers(objectIdentifiers);
    }

    private boolean removeS3Objects(Collection<S3Object> s3Objects) {
        if (s3Objects == null) {
            throw new IllegalArgumentException();
        }
        if (s3Objects.isEmpty()) {
            return true;
        }
        List<ObjectIdentifier> objectIdentifiers = s3Objects.stream()
                .map(a -> ObjectIdentifier.builder().key(a.key()).build())
                .collect(Collectors.toList());
        return removeObjectIdentifiers(objectIdentifiers);
    }

    private boolean removeObjectIdentifiers(Collection<ObjectIdentifier> objectIdentifiers) {
        if (objectIdentifiers == null) {
            throw new IllegalArgumentException();
        }
        if (objectIdentifiers.isEmpty()) {
            return true;
        }
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
    public boolean copy(String path, String dstPath, boolean recursive) {
        return false;
    }

    @Override
    public boolean move(String path, String dstPath) {
        return false;
    }

    @Override
    public byte[] load(String path) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();
        return s3Client.getObjectAsBytes(request).asByteArray();
    }

    @Override
    public boolean save(String path, byte[] bytes) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();
        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromBytes(bytes));
        return response.sdkHttpResponse().isSuccessful();
    }

    public boolean save(String path, File file) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();
        PutObjectResponse response = s3Client.putObject(request, RequestBody.fromFile(file));
        return response.sdkHttpResponse().isSuccessful();
    }
}
