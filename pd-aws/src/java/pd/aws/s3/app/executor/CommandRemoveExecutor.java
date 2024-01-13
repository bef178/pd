package pd.aws.s3.app.executor;

import java.util.Arrays;
import java.util.stream.Collectors;

import pd.aws.s3.AwsS3Accessor;
import pd.aws.s3.app.CommandKey;
import pd.aws.s3.app.ParamKey;
import pd.util.ParamManager;

import static pd.util.AppLogger.stdout;
import static pd.util.AppLogger.stdoutNoNewLine;

public class CommandRemoveExecutor extends CommandExecutor {

    public CommandRemoveExecutor() {
        super(CommandKey.remove, Arrays.stream(new ParamKey[] { ParamKey.remote_key }).collect(Collectors.toList()));
    }

    @Override
    public void execute(ParamManager paramManager) {
        String remoteKey = paramManager.get(paramKeys.get(0));
        checkNotNullAndNotEmpty(remoteKey);
        AwsS3Accessor accessor = checkAndCreateAccessor(paramManager);

        stdoutNoNewLine("removing: {} ... ", remoteKey);
        if (accessor.remove(remoteKey)) {
            stdout("done");
        } else {
            stdout("failed");
            throw new RuntimeException();
        }
    }
}
