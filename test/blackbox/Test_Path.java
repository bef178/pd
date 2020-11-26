package blackbox;

import java.util.LinkedHashMap;
import java.util.Map;

import pd.net.uri.Path;

public class Test_Path {

    // vm arguments: -ea
    public static void main(String[] args) {
        test_getBasename();
        test_getParent();
        test_normalize();

        System.out.println("all done");
    }

    private static void test_getBasename() {
        LinkedHashMap<String, String> testcases = new LinkedHashMap<>();
        testcases.put("abc", "abc");
        testcases.put("abc/def", "def");
        testcases.put("abc///", "abc");
        testcases.put("abc//.", ".");
        testcases.put("abc//.///", ".");
        testcases.put("////", "/");
        testcases.put("", "");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = Path.getBasename(input);
            assert actual.equals(expected) : String.format("E: check %s: input[%s], expected[%s], actual[%s]",
                    "Path.getBasename", input, expected, actual);
        }
    }

    private static void test_getParent() {
        LinkedHashMap<String, String> testcases = new LinkedHashMap<>();
        testcases.put("abc", ".");
        testcases.put("abc//", ".");
        testcases.put("abc/../def", "abc/..");
        testcases.put("/abc", "/");
        testcases.put("/", "/");
        testcases.put("abc//.///", "abc");
        testcases.put("", ".");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = Path.getParent(input);
            assert actual.equals(expected) : String.format("E: check %s: input[%s], expected[%s], actual[%s]",
                    "Path.getParent", input, expected, actual);
        }
    }

    private static void test_normalize() {
        LinkedHashMap<String, String> testcases = new LinkedHashMap<>();
        testcases.put("abc", "./abc");
        testcases.put("././abc", "./abc");
        testcases.put("./../abc", "../abc");
        testcases.put("/../abc", "/abc");
        testcases.put("../.././../abc/..", "../../..");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = Path.normalize(input);
            assert actual.equals(expected) : String.format("E: check %s: input[%s], expected[%s], actual[%s]",
                    "Path.normalize", input, expected, actual);
        }
    }
}
