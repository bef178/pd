package pd.aws.s3.app.executor;

import java.util.List;

import pd.aws.s3.AwsS3Accessor;
import pd.aws.s3.app.CommandKey;
import pd.aws.s3.app.ParamKey;
import pd.util.ParamManager;

public abstract class CommandExecutor {

    public final CommandKey commandKey;

    public final List<ParamKey> paramKeys;

    public CommandExecutor(CommandKey commandKey, List<ParamKey> paramKeys) {
        this.commandKey = commandKey;
        this.paramKeys = paramKeys;
    }

    public abstract void execute(ParamManager paramManager);

    protected AwsS3Accessor checkAndCreateAccessor(ParamManager config) {
        final String accessId = config.get(ParamKey.access_id);
        final String accessSecret = config.get(ParamKey.access_secret);
        final String regionName = config.get(ParamKey.region_name);
        final String endpointUrl = config.get(ParamKey.endpoint_url);
        final String bucketName = config.get(ParamKey.bucket_name);
        checkNotNullAndNotEmpty(accessId, accessSecret, regionName, endpointUrl, bucketName);
        return new AwsS3Accessor(accessId, accessSecret, regionName, endpointUrl, bucketName);
    }

    protected void checkNotNull(String... values) {
        for (String value : values) {
            if (value == null) {
                throw new IllegalArgumentException();
            }
        }
    }

    protected void checkNotNullAndNotEmpty(String... values) {
        for (String value : values) {
            if (value == null || value.isEmpty()) {
                throw new IllegalArgumentException();
            }
        }
    }
}
