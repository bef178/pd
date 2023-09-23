package pd.app.fdup;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import pd.codec.GetOpt;
import pd.codec.Md5Codec;
import pd.fenc.CurvePattern;
import pd.file.FileStat;
import pd.file.LocalFileAccessor;

public class Main {

    public static final String COMMAND_LIST = "list";
    public static final String COMMAND_LIST_DUPLICATED = "list-duplicated";
    public static final String COMMAND_REMOVE_DUPLICATED = "remove-duplicated";

    public static final LocalFileAccessor accessor = LocalFileAccessor.singleton();

    public static void main(String[] args) {
        List<Map.Entry<String, String>> params;
        try {
            params = GetOpt.parse("--command:", args);
        } catch (Exception e) {
            stderr("{}", e);
            System.exit(1);
            return;
        }

        String command = params.stream()
                .filter(a -> Objects.equals(a.getKey(), "--command"))
                .map(Map.Entry::getValue)
                .reduce((a, b) -> b)
                .orElse("");
        List<String> paths = params.stream()
                .filter(a -> Objects.equals(a.getKey(), "!opt"))
                .map(a -> {
                    String path = a.getValue();
                    if (path.startsWith("\"") && path.endsWith("\"")) {
                        path = path.substring(1, path.length() - 1);
                    }
                    return path;
                })
                .collect(Collectors.toList());

        switch (command) {
            case COMMAND_LIST:
            case COMMAND_LIST_DUPLICATED:
            case COMMAND_REMOVE_DUPLICATED:
                groupFilesAndExecute(paths, command);
                break;
            default:
                stderr("unknown command: `{}`", command);
                System.exit(1);
                break;
        }
    }

    public static void groupFilesAndExecute(List<String> paths, String command) {
        if (paths == null) {
            throw new IllegalArgumentException();
        }

        if (paths.isEmpty()) {
            return;
        }

        List<FileStat> stats = paths.stream()
                .flatMap(a -> accessor.statAllRegularFiles(a).stream())
                .collect(Collectors.toList());
        stdout("found {} file(s)", stats.size());

        List<List<FileStat>> sizeGroupedFiles = stats.stream()
                .collect(Collectors.groupingBy(a -> a.size))
                .entrySet().stream()
                .sorted(Comparator.comparingLong(Map.Entry::getKey))
                .filter(a -> a.getKey() > 0)
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
                byte[] bytes = accessor.load(stat.path);
                String checksum = Md5Codec.md5sum(bytes);
                if (!sameHashFiles.containsKey(checksum)) {
                    sameHashFiles.put(checksum, new LinkedList<>());
                }
                sameHashFiles.get(checksum).add(stat);
            }
            for (List<FileStat> group : sameHashFiles.values()) {
                executeCommand(command, group);
            }
        });
    }

    private static void executeCommand(String command, List<FileStat> group) {
        switch (command) {
            case COMMAND_LIST:
                for (FileStat stat : group) {
                    stdout(stat.path);
                }
                stdout("");
                break;
            case COMMAND_LIST_DUPLICATED:
                if (group.size() > 1) {
                    for (int i = 1; i < group.size(); i++) {
                        FileStat stat = group.get(i);
                        stdout(stat.path);
                    }
                    stdout("");
                }
                break;
            case COMMAND_REMOVE_DUPLICATED:
                if (group.size() > 1) {
                    FileStat stat = group.get(0);
                    stdout("keep {}", stat.path);
                    for (int i = 1; i < group.size(); i++) {
                        stat = group.get(i);
                        if (accessor.removeRegularFile(stat.path)) {
                            stdout("removed {}", stat.path);
                        }
                    }
                    stdout("");
                }
                break;
            default:
                stderr("unknown command `{}`", command);
                break;
        }
    }

    private static void stdout(String message, Object... messageParams) {
        System.out.println(CurvePattern.format(message, messageParams));
    }

    private static void stderr(String message, Object... messageParams) {
        System.err.println(CurvePattern.format(message, messageParams));
    }
}
