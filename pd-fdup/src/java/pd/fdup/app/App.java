package pd.fdup.app;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import pd.fstore.FileStat;
import pd.fstore.LocalFileAccessor;
import pd.util.DigestEncoder;
import pd.util.ParamManager;

import static pd.util.AppLogger.stderr;
import static pd.util.AppLogger.stdout;

public class App {

    public static final LocalFileAccessor accessor = new LocalFileAccessor();

    private static final DigestEncoder md5Digest = new DigestEncoder(DigestEncoder.Algorithm.md5);

    public static void main(String[] args) {
        ParamManager paramManager;
        try {
            String optString = Arrays.stream(ParamKey.values()).map(ParamKey::toOptString).collect(Collectors.joining(","));
            paramManager = ParamManager.parse(optString, args);
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

        List<String> paths = paramManager.getNonOptionArguments().stream()
                .map(path -> {
                    if (path.startsWith("\"") && path.endsWith("\"")) {
                        path = path.substring(1, path.length() - 1);
                    }
                    if (path.equals(".")) {
                        return "";
                    }
                    return path;
                })
                .collect(Collectors.toList());

        try {
            groupFilesAndExecuteCommand(paths, commandKey);
            System.exit(0);
        } catch (IllegalArgumentException e) {
            usage();
            System.exit(1);
        } catch (Exception e) {
            if (e.getMessage() != null) {
                stderr(e.getMessage());
            } else {
                stderr(e.getClass().getName());
            }
            System.exit(1);
        }
    }

    private static void groupFilesAndExecuteCommand(List<String> paths, CommandKey command) {
        if (paths == null) {
            throw new IllegalArgumentException();
        }

        if (paths.isEmpty()) {
            return;
        }

        List<FileStat> stats = paths.stream()
                .flatMap(a -> {
                    List<String> b = accessor.listAll(a);
                    return b == null ? Stream.empty() : b.stream();
                })
                .map(accessor::stat)
                .collect(Collectors.toList());
        stdout("found {} file(s)", stats.size());

        List<List<FileStat>> sizeGroupedFiles = stats.stream()
                .filter(a -> a != null && a.contentLength > 0)
                .collect(Collectors.groupingBy(a -> a.contentLength))
                .entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getKey))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
        stdout("found {} group(s) by size", sizeGroupedFiles.size());

        sizeGroupedFiles.forEach(a -> {
            if (a.size() == 1) {
                executeCommand(command, a);
                return;
            }

            Map<String, List<FileStat>> sameHashFiles = new LinkedHashMap<>();
            for (FileStat stat : a) {
                String checksum;
                try (InputStream inputStream = Files.newInputStream(Paths.get(stat.key))) {
                    checksum = md5Digest.checksum(inputStream);
                } catch (IOException e) {
                    checksum = null;
                }
                if (checksum != null) {
                    if (!sameHashFiles.containsKey(checksum)) {
                        sameHashFiles.put(checksum, new LinkedList<>());
                    }
                    sameHashFiles.get(checksum).add(stat);
                }
            }
            for (List<FileStat> group : sameHashFiles.values()) {
                executeCommand(command, group);
            }
        });
    }

    private static void executeCommand(CommandKey commandKey, List<FileStat> group) {
        if (group.isEmpty()) {
            return;
        }
        switch (commandKey) {
            case list:
                stdout("");
                stdout("size: {}", group.get(0).contentLength);
                for (FileStat stat : group) {
                    stdout(stat.key);
                }
                stdout("");
                break;
            case list_duplicated:
                if (group.size() > 1) {
                    stdout("");
                    stdout("size: {}", group.get(0).contentLength);
                    for (int i = 1; i < group.size(); i++) {
                        FileStat stat = group.get(i);
                        stdout(stat.key);
                    }
                }
                break;
            case remove_duplicated:
                if (group.size() > 1) {
                    stdout("");
                    stdout("size: {}", group.get(0).contentLength);
                    FileStat stat = group.get(0);
                    stdout("o {}", stat.key);
                    for (int i = 1; i < group.size(); i++) {
                        stat = group.get(i);
                        if (accessor.remove(stat.key)) {
                            stdout("x {}", stat.key);
                        }
                    }
                }
                break;
            default:
                stderr("Unknown commandKey `{}`", commandKey);
                break;
        }
    }

    private static void usage() {
        StringBuilder sb = new StringBuilder();
        sb.append("usage: $0 --command <command> <path-or-prefix> ...\n");
        sb.append("\".\" stands for current directory, \"..\" is not supported");
        sb.append("where <command> is one of\n");
        for (CommandKey commandKey : CommandKey.values()) {
            sb.append('\t').append(commandKey.name()).append('\n');
        }
        stdout(sb.toString());
    }
}
