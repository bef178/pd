package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pd.util.PathExtension.getBasename;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import pd.util.PathExtension;

public class Test_PathExtension {

    @Test
    public void test_getBasename() {
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
            String actual = getBasename(input);
            assertEquals(expected, actual, String.format("input `%s`", input));
        }
    }

    @Test
    public void test_getBasename_fail() {
        assertThrows(IllegalArgumentException.class, () -> {
            getBasename("");
        });
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
        testcases.put(".", ".");

        for (Map.Entry<String, String> testcase : testcases.entrySet()) {
            String input = testcase.getKey();
            String expected = testcase.getValue();
            String actual = PathExtension.getParent(input);
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
        assertEquals("a/b", PathExtension.resolve("a/", "b"));
        assertEquals("a///b", PathExtension.resolve("a///", "b"));
    }
}
