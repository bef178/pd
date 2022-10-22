package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import pd.util.PathUtil;

public class Test_PathUtil {

    @Test
    public void test_basename() {
        LinkedHashMap<String, String> testcases = new LinkedHashMap<>();
        testcases.put("abc", "abc");
        testcases.put("abc/def", "def");
        testcases.put("abc///", "abc");
        testcases.put("abc//.", ".");
        testcases.put("//.///", ".");
        testcases.put("////", "/");
        testcases.put("/", "/");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = PathUtil.basename(input);
            assertEquals(expected, actual, String.format("input `%s`", input));
        }
    }

    @Test
    public void test_basename_fail() {
        assertThrows(IllegalArgumentException.class, () -> {
            PathUtil.basename("");
        });
    }

    @Test
    public void test_parent() {
        LinkedHashMap<String, String> testcases = new LinkedHashMap<>();
        testcases.put("abc", ".");
        testcases.put("abc//", ".");
        testcases.put("abc/../def", "abc/..");
        testcases.put("/abc", "/");
        testcases.put("/", "/");
        testcases.put("abc//.///", "abc");
        testcases.put(".", ".");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = PathUtil.parent(input);
            assertEquals(expected, actual, String.format("input `%s`", input));
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
        testcases.put("/", "/");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = PathUtil.normalize(input);
            assertEquals(expected, actual, String.format("input `%s`", input));
        }
    }

    @Test
    public void test_resolve() {
        assertEquals("a/b", PathUtil.resolve("a", "b"));
        assertEquals("/b", PathUtil.resolve("a", "/b"));
        assertEquals("a/b", PathUtil.resolve("a/", "b"));
        assertEquals("a///b", PathUtil.resolve("a///", "b"));
    }
}
