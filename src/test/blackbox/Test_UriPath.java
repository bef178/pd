package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import pd.util.PathExtension;

public class Test_UriPath {

    @Test
    public void test_getBasename() {
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
            String actual = PathExtension.getBasename(input);
            assertEquals(expected, actual, String.format(
                    "E: check %s: input[%s], expected[%s], actual[%s]",
                    "Path.getBasename", input, expected, actual));
        }
    }

    @Test
    public void test_getParent() {
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
            String actual = PathExtension.getParent(input);
            assertEquals(expected, actual, String.format(
                    "E: check %s: input[%s], expected[%s], actual[%s]",
                    "Path.getParent", input, expected, actual));
        }
    }

    @Test
    public void test_normalize() {
        LinkedHashMap<String, String> testcases = new LinkedHashMap<>();
        testcases.put("abc", "./abc");
        testcases.put("././abc", "./abc");
        testcases.put("./../abc", "../abc");
        testcases.put("/../abc", "/abc");
        testcases.put("../.././../abc/..", "../../..");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = PathExtension.normalize(input);
            assertEquals(expected, actual, String.format(
                    "E: check %s: input[%s], expected[%s], actual[%s]",
                    "Path.normalize", input, expected, actual));
        }
    }
}
