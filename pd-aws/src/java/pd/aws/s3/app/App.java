package pd.aws.s3.app;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import pd.aws.s3.app.executor.CommandDownloadAllExecutor;
import pd.aws.s3.app.executor.CommandDownloadExecutor;
import pd.aws.s3.app.executor.CommandExecutor;
import pd.aws.s3.app.executor.CommandListAllExecutor;
import pd.aws.s3.app.executor.CommandListExecutor;
import pd.aws.s3.app.executor.CommandRemoveAllExecutor;
import pd.aws.s3.app.executor.CommandRemoveExecutor;
import pd.aws.s3.app.executor.CommandUploadAllExecutor;
import pd.aws.s3.app.executor.CommandUploadExecutor;
import pd.util.ParamManager;

import static pd.util.AppLogger.stderr;
import static pd.util.AppLogger.stdout;

public class App {

    public static void main(String[] args) {
        List<CommandExecutor> executors = Arrays.asList(
                new CommandListExecutor(),
                new CommandListAllExecutor(),
                new CommandDownloadExecutor(),
                new CommandDownloadAllExecutor(),
                new CommandUploadExecutor(),
                new CommandUploadAllExecutor(),
                new CommandRemoveExecutor(),
                new CommandRemoveAllExecutor());
        new App(executors).execute(args);
    }

    private final List<CommandExecutor> executors;

    public App(List<CommandExecutor> executors) {
        this.executors = executors;
    }

    public void execute(String[] args) {
        ParamManager paramManager;
        try {
            String optString = Arrays.stream(ParamKey.values()).map(ParamKey::toOptString).collect(Collectors.joining(","));
            paramManager = ParamManager.parse(optString, args, ParamKey.config, null);
        } catch (Exception e) {
            stderr("{}", e);
            System.exit(1);
            return;
        }

        final CommandKey commandKey = CommandKey.fromLiteral(paramManager.get(ParamKey.command));
        if (commandKey == null) {
            usage();
            System.exit(1);
            return;
        }

        CommandExecutor executor = executors.stream()
                .filter(a -> a.commandKey == commandKey)
                .findFirst().orElse(null);
        if (executor == null) {
            usage();
            System.exit(1);
            return;
        }

        try {
            executor.execute(paramManager);
            System.exit(0);
        } catch (IllegalArgumentException e) {
            usage();
            System.exit(1);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                stderr(e.getMessage());
            }
            System.exit(1);
        }
    }

    public void usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("usage: $0 <config> <command-and-arguments>\n");
        sb.append("where <config> is one of\n");
        appendOptPairUsage(sb, "\t", ParamKey.config.name(), "path-to-config");
        sb.append('\n');
        appendOptPairUsage(sb, "\t", ParamKey.access_id.name(), ParamKey.access_id.name());
        appendOptPairUsage(sb, " ", ParamKey.access_secret.name(), ParamKey.access_secret.name());
        appendOptPairUsage(sb, " ", ParamKey.region_name.name(), ParamKey.region_name.name());
        appendOptPairUsage(sb, " ", ParamKey.endpoint_url.name(), ParamKey.endpoint_url.name());
        appendOptPairUsage(sb, " ", ParamKey.bucket_name.name(), ParamKey.bucket_name.name());
        sb.append('\n');
        sb.append("where <command-and-arguments> is one of\n");
        for (CommandExecutor commandExecutor : executors) {
            sb.append('\t').append("--").append(ParamKey.command.name()).append(' ').append(commandExecutor.commandKey.name());
            for (ParamKey paramKey : commandExecutor.paramKeys) {
                appendOptPairUsage(sb, " ", paramKey.name(), paramKey.name());
            }
            sb.append('\n');
        }
        stdout(sb.toString());
    }

    private static void appendOptPairUsage(StringBuilder sb, String prefix, String key, String value) {
        sb.append(prefix).append("--").append(key).append(' ').append('<').append(value).append('>');
    }
}
