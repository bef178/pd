package pd.aws.s3;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import pd.fenc.CurlyBracketPatternExtension;
import pd.fstore.LocalFileAccessor;

@Slf4j
public class Main {

    public static void main(String[] args) {
        Map<String, String> params;
        Map<String, String> configParams;
        try {
            params = pickParams(args);
            configParams = pickConfigParams(params.getOrDefault("config-file", null));
        } catch (Exception e) {
            stderr("{}", e);
            System.exit(1);
            return;
        }

        final String accessKeyId = params.getOrDefault("access-key-id", configParams.getOrDefault("accessKeyId", System.getenv("accessKeyId")));
        final String secretAccessKey = params.getOrDefault("secret-access-key", configParams.getOrDefault("secretAccessKey", System.getenv("secretAccessKey")));
        final String regionName = params.getOrDefault("region-name", configParams.getOrDefault("regionName", System.getenv("regionName")));
        final String endpointUrl = params.getOrDefault("endpoint-url", configParams.getOrDefault("endpointUrl", System.getenv("endpointUrl")));
        final String bucketName = params.getOrDefault("bucket-name", configParams.getOrDefault("bucketName", System.getenv("bucketName")));

        String action = params.getOrDefault("action", "");
        switch (action) {
            case "list2": {
                AwsS3Accessor accessor = new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
                int exitCode = list2(accessor, params.get("remote-prefix"));
                System.exit(exitCode);
                break;
            }
            case "download-remote-directory": {
                AwsS3Accessor accessor = new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
                int exitCode = downloadRemoteDirectory(accessor, params.get("remote-directory"), params.get("local-directory"));
                System.exit(exitCode);
                break;
            }
            case "upload-to-remote-directory": {
                AwsS3Accessor accessor = new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
                int exitCode = uploadToRemoteDirectory(accessor, params.get("remote-directory"), params.get("local-directory"));
                System.exit(exitCode);
                break;
            }
            case "remove-remote-directory": {
                AwsS3Accessor accessor = new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
                int exitCode = removeRemoteDirectory(accessor, params.get("remote-directory"));
                System.exit(exitCode);
                break;
            }
            case "download-remote-file": {
                AwsS3Accessor accessor = new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
                int exitCode = downloadRemoteFile(accessor, params.get("remote-file"), params.get("local-file"));
                System.exit(exitCode);
                break;
            }
            case "upload-to-remote-file": {
                AwsS3Accessor accessor = new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
                int exitCode = uploadToRemoteFile(accessor, params.get("remote-file"), params.get("local-file"));
                System.exit(exitCode);
                break;
            }
            case "remove-remote-file": {
                AwsS3Accessor accessor = new AwsS3Accessor(accessKeyId, secretAccessKey, regionName, endpointUrl, bucketName);
                int exitCode = removeRemoteFile(accessor, params.get("remote-file"));
                System.exit(exitCode);
                break;
            }
            default:
                stderr("Unknown action: `{}`", action);
                System.exit(1);
                break;
        }
    }

    public static int list2(AwsS3Accessor accessor, String remotePrefix) {
        List<String> paths = accessor.list(remotePrefix);
        stdout("{}", paths);
        return 1;
    }

    public static int downloadRemoteDirectory(AwsS3Accessor accessor, String remoteDirectory, String localDirectory) {
        stdout("downloadRemoteDirectory: remote:{} => {}", remoteDirectory, localDirectory);

        if (!checkDirectory(remoteDirectory, localDirectory)) {
            return 1;
        }

        List<String> s3Keys = accessor.listAll(remoteDirectory);
        stdout("find {} s3Key(s)", s3Keys.size());

        int numErrors = s3Keys.parallelStream().mapToInt(s3Key -> {
            if (!s3Key.endsWith("/")) {
                String dstPath = s3Key.replaceFirst(remoteDirectory, localDirectory);
                try {
                    downloadRemoteFile(accessor, s3Key, dstPath);
                } catch (Exception e) {
                    stderr("Failed to downloadFile " + s3Key + " => " + dstPath, e);
                    return 1;
                }
            }
            return 0;
        }).sum();
        stdout("errors: {}", numErrors);

        return numErrors == 0 ? 0 : 1;
    }

    public static int uploadToRemoteDirectory(AwsS3Accessor accessor, String remoteDirectory, String localDirectory) {
        stdout("uploadToRemoteDirectory: {} => remote:{}", localDirectory, remoteDirectory);

        if (!checkDirectory(remoteDirectory, localDirectory)) {
            return 1;
        }

        List<String> localFiles = LocalFileAccessor.singleton().listAll(localDirectory);
        stdout("find {} localFile(s)", localFiles.size());

        localFiles.parallelStream().forEach(localFile -> {
            String s3Key = localFile.replaceFirst(localDirectory, remoteDirectory);
            accessor.upload(s3Key, localFile);
        });
        return 0;
    }

    private static boolean checkDirectory(String remoteDirectory, String localDirectory) {
        if (!remoteDirectory.endsWith("/")) {
            stderr("remote directory should end with '/'");
            return false;
        } else if (!localDirectory.endsWith("/")) {
            stderr("local directory should end with '/'");
            return false;
        }

        File localDirectoryFile = new File(localDirectory);
        if (localDirectoryFile.exists() && !localDirectoryFile.isDirectory()) {
            stderr("local directory exists but is not a directory: " + localDirectory);
            return false;
        }
        return true;
    }

    public static int removeRemoteDirectory(AwsS3Accessor accessor, String remoteDirectory) {
        stdout("removeRemoteDirectory: {}", remoteDirectory);
        if (!remoteDirectory.endsWith("/")) {
            remoteDirectory += "/";
        }

        boolean isSuccessful = accessor.removeAll(remoteDirectory);
        if (isSuccessful) {
            stdout("removed");
        } else {
            stderr("failed to remove");
            return 1;
        }
        return 0;
    }

    @SneakyThrows
    public static int downloadRemoteFile(AwsS3Accessor accessor, String remoteFile, String localParity) {
        stdout("downloadRemoteFile: remote:{} => {}", remoteFile, localParity);

        byte[] bytes = accessor.load(remoteFile);
        Files.write(Paths.get(localParity), bytes);
        stdout("downloaded");
        return 0;
    }

    public static int uploadToRemoteFile(AwsS3Accessor accessor, String remoteFile, String localParity) {
        stdout("uploadToRemoteFile: {} => remote:{}", localParity, remoteFile);

        boolean isSuccessful = accessor.upload(remoteFile, localParity);
        if (isSuccessful) {
            stdout("uploaded");
        } else {
            stderr("failed to upload");
            return 1;
        }
        return 0;
    }

    public static int removeRemoteFile(AwsS3Accessor accessor, String remoteFile) {
        stdout("removeRemoteFile: {}", remoteFile);

        boolean isSuccessful = accessor.remove(remoteFile);
        if (isSuccessful) {
            stdout("removed");
        } else {
            stderr("failed to remove");
            return 1;
        }
        return 0;
    }

    private static Map<String, String> pickParams(String[] args) {
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                throw new RuntimeException("argument should start with `--`");
            }
            int i = arg.indexOf('=');
            if (i == -1) {
                params.put(arg.substring(2), "");
            } else {
                params.put(arg.substring(2, i), arg.substring(i + 1));
            }
        }
        return params;
    }

    @SneakyThrows
    private static Map<String, String> pickConfigParams(String configPath) {
        if (configPath == null) {
            return Collections.emptyMap();
        }
        Properties properties = new Properties();
        try (InputStream inputStream = Files.newInputStream(Paths.get(configPath))) {
            properties.load(inputStream);
        }
        return properties.entrySet().stream()
                .collect(Collectors.toMap(
                        a -> (String) a.getKey(),
                        a -> (String) a.getValue(),
                        (prev, next) -> next,
                        LinkedHashMap::new
                ));
    }

    private static void stdout(String message, Object... messageParams) {
        System.out.println(CurlyBracketPatternExtension.format(message, messageParams));
    }

    private static void stderr(String message, Object... messageParams) {
        System.err.println(CurlyBracketPatternExtension.format(message, messageParams));
    }
}
