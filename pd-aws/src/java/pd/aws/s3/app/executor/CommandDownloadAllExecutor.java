package pd.aws.s3.app.executor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import pd.aws.s3.AwsS3Accessor;
import pd.aws.s3.app.CommandKey;
import pd.aws.s3.app.ParamKey;
import pd.util.ParamManager;

import static pd.util.AppLogger.stdout;

public class CommandDownloadAllExecutor extends CommandExecutor {

    public CommandDownloadAllExecutor() {
        super(CommandKey.download_all, Arrays.stream(new ParamKey[] { ParamKey.remote_prefix, ParamKey.prefix }).collect(Collectors.toList()));
    }

    @Override
    public void execute(ParamManager paramManager) {
        final String remotePrefix = paramManager.get(paramKeys.get(0));
        final String localParity = paramManager.get(paramKeys.get(1));
        checkNotNull(remotePrefix, localParity);
        AwsS3Accessor accessor = checkAndCreateAccessor(paramManager);

        List<String> remoteKeys = accessor.listAll(remotePrefix);
        stdout("download: find {} key(s)", remoteKeys.size());

        AtomicBoolean allSuccessful = new AtomicBoolean(true);
        stdout("downloading {}* => {}*", remotePrefix, localParity);
        remoteKeys.parallelStream().forEach(key -> {
            if (!allSuccessful.get()) {
                return;
            }
            String localPath = localParity + key.substring(remotePrefix.length());
            try {
                accessor.download(key, localPath);
            } catch (Exception e) {
                allSuccessful.set(false);
                stdout("download: {} => {} ... failed", localPath, key);
            }
        });
        if (!allSuccessful.get()) {
            throw new RuntimeException();
        }
    }
}
