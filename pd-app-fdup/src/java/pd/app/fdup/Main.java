package pd.app.fdup;

import java.security.MessageDigest;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.SneakyThrows;
import pd.codec.HexCodec;
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
            params = pickParams(args);
        } catch (Exception e) {
            stderr("{}", e);
            System.exit(1);
            return;
        }

        List<String> paths = params.stream().filter(a -> Objects.equals(a.getKey(), "input-file")).map(Map.Entry::getValue).collect(Collectors.toList());
        String command = params.stream().filter(a -> Objects.equals(a.getKey(), "command")).map(Map.Entry::getValue).reduce((a, b) -> b).orElse("");

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
                .entrySet()
                .stream()
                .sorted((a, b) -> Math.toIntExact(a.getKey() - b.getKey()))
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
                String checksum = md5sum(bytes);
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
                    for (int i = 1; i < group.size(); i++) {
                        FileStat stat = group.get(i);
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

    @SneakyThrows
    public static String md5sum(byte[] bytes) {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] digest = md5.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            int[] a = new int[2];
            HexCodec.encode1byte(digest[i], a, 0);
            sb.appendCodePoint(a[0]).appendCodePoint(a[1]);
        }
        return sb.toString().toLowerCase();
    }

    private static List<Map.Entry<String, String>> pickParams(String[] args) {
        List<Map.Entry<String, String>> params = new LinkedList<>();
        for (String arg : args) {
            if (!arg.startsWith("--")) {
                throw new RuntimeException("argument should start with `--`");
            }
            int i = arg.indexOf('=');
            if (i == -1) {
                params.add(new AbstractMap.SimpleImmutableEntry<>(arg.substring(2), ""));
            } else {
                params.add(new AbstractMap.SimpleImmutableEntry<>(arg.substring(2, i), arg.substring(i + 1)));
            }
        }
        return params;
    }

    private static void stdout(String message, Object... messageParams) {
        System.out.println(CurvePattern.format(message, messageParams));
    }

    private static void stderr(String message, Object... messageParams) {
        System.err.println(CurvePattern.format(message, messageParams));
    }
}
