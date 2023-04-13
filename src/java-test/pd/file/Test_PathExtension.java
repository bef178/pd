package pd.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class Test_PathExtension {

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
            String actual = PathExtension.basename(input);
            assertEquals(expected, actual, String.format("input `%s`", input));
        }
    }

    @Test
    public void test_basename_fail() {
        assertThrows(IllegalArgumentException.class, () -> {
            PathExtension.basename("");
        });
    }

    @Test
    public void test_dirname() {
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
            String actual = PathExtension.dirname(input);
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
            String actual = PathExtension.normalize(input);
            assertEquals(expected, actual, String.format("input `%s`", input));
        }
    }

    @Test
    public void test_resolve() {
        assertEquals("a/b", PathExtension.resolve("a", "b"));
        assertEquals("/b", PathExtension.resolve("a", "/b"));
        assertEquals("a///b", PathExtension.resolve("a//", "b"));
        assertEquals("a//b/c", PathExtension.resolve("a/", "b", "c"));
        assertEquals("//c", PathExtension.resolve("a/", "b", "//c"));
    }
}
