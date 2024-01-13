package pd.aws.s3.app.executor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import pd.aws.s3.AwsS3Accessor;
import pd.aws.s3.app.CommandKey;
import pd.aws.s3.app.ParamKey;
import pd.util.ParamManager;

import static pd.util.AppLogger.stdout;

public class CommandListAllExecutor extends CommandExecutor {

    public CommandListAllExecutor() {
        super(CommandKey.list_all, Arrays.stream(new ParamKey[] { ParamKey.remote_prefix }).collect(Collectors.toList()));
    }

    @Override
    public void execute(ParamManager paramManager) {
        final String remotePrefix = paramManager.get(paramKeys.get(0));
        checkNotNull(remotePrefix);
        AwsS3Accessor accessor = checkAndCreateAccessor(paramManager);
        List<String> paths = accessor.listAll(remotePrefix);
        stdout("{}", paths);
    }
}
