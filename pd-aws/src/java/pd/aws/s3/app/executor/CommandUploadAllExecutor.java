package pd.aws.s3.app.executor;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import pd.aws.s3.AwsS3Accessor;
import pd.aws.s3.app.CommandKey;
import pd.aws.s3.app.ParamKey;
import pd.fstore.LocalFileAccessor;
import pd.util.ParamManager;

import static pd.util.AppLogger.stdout;

public class CommandUploadAllExecutor extends CommandExecutor {

    private final LocalFileAccessor localFileAccessor = new LocalFileAccessor();

    public CommandUploadAllExecutor() {
        super(CommandKey.upload_all, Arrays.stream(new ParamKey[] { ParamKey.remote_prefix, ParamKey.prefix }).collect(Collectors.toList()));
    }

    @Override
    public void execute(ParamManager paramManager) {
        final String remotePrefix = paramManager.get(paramKeys.get(0));
        final String localParity = paramManager.get(paramKeys.get(1));
        checkNotNull(remotePrefix, localParity);
        AwsS3Accessor accessor = checkAndCreateAccessor(paramManager);

        List<String> localFiles = localFileAccessor.listAll(localParity);
        stdout("upload: find {} file(s)", localFiles.size());

        AtomicBoolean allSuccessful = new AtomicBoolean(true);
        stdout("uploading: {}* => {}*", localParity, remotePrefix);
        localFiles.parallelStream().forEach(p -> {
            if (!allSuccessful.get()) {
                return;
            }
            String remoteKey = remotePrefix + p.substring(localParity.length());
            if (!accessor.upload(remoteKey, p)) {
                allSuccessful.set(false);
                stdout("upload: {} => {} ... failed", p, remoteKey);
            }
        });
        if (!allSuccessful.get()) {
            throw new RuntimeException();
        }
    }
}
