package pd.aws.s3.app.executor;

import java.util.Arrays;
import java.util.stream.Collectors;

import pd.aws.s3.AwsS3Accessor;
import pd.aws.s3.app.CommandKey;
import pd.aws.s3.app.ParamKey;
import pd.util.ParamManager;

import static pd.util.AppLogger.stdout;
import static pd.util.AppLogger.stdoutNoNewLine;

public class CommandUploadExecutor extends CommandExecutor {

    public CommandUploadExecutor() {
        super(CommandKey.upload, Arrays.stream(new ParamKey[] { ParamKey.remote_key, ParamKey.key }).collect(Collectors.toList()));
    }

    @Override
    public void execute(ParamManager paramManager) {
        final String remoteKey = paramManager.get(paramKeys.get(0));
        final String localParity = paramManager.get(paramKeys.get(1));
        checkNotNullAndNotEmpty(remoteKey, localParity);
        AwsS3Accessor accessor = checkAndCreateAccessor(paramManager);

        stdoutNoNewLine("uploading: {} => {} ... ", localParity, remoteKey);
        if (accessor.upload(remoteKey, localParity)) {
            stdout("done");
        } else {
            stdout("failed");
            throw new RuntimeException();
        }
    }
}
