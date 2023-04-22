package pd.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class Test_PathExtension {

    @Test
    public void test_basename() {
        test_basename("abc", "abc");
        test_basename("def/abc", "abc");
        test_basename("abc///", "abc");
        test_basename("abc//.", ".");
        test_basename("//.///", ".");
        test_basename("////", "/");
        test_basename("/", "/");

        test_basename("a/b/c.d", ".d", "c");
        test_basename("a/b/c.d", "c.d", "c.d");
    }

    private void test_basename(String input, String expected) {
        test_basename(input, null, expected);
    }

    private void test_basename(String input, String inputSuffix, String expected) {
        String actual = PathExtension.basename(input, inputSuffix);
        assertEquals(expected, actual, String.format("E: input: `%s`", input));
    }

    @Test
    public void test_basename_fail() {
        assertThrows(IllegalArgumentException.class, () -> {
            PathExtension.basename("");
        });
    }

    @Test
    public void test_dirname() {
        test_dirname("abc", ".");
        test_dirname("abc//", ".");
        test_dirname("abc/../def", "abc/..");
        test_dirname("/abc", "/");
        test_dirname("/", "/");
        test_dirname("abc//.///", "abc");
        test_dirname(".", ".");
    }

    private void test_dirname(String input, String expected) {
        String actual = PathExtension.dirname(input);
        assertEquals(expected, actual, String.format("E: input: `%s`", input));
    }

    @Test
    public void test_extname() {
        test_extname("/tmp/a.b.c", "c");
        test_extname("/tmp/a", "");
    }

    private void test_extname(String input, String expected) {
        String actual = PathExtension.extname(input);
        assertEquals(expected, actual, String.format("E: input: `%s`", input));
    }

    @Test
    public void test_normalize() {
        test_normalize("abc", "./abc");
        test_normalize("././abc", "./abc");
        test_normalize("./../abc", "../abc");
        test_normalize("/../abc", "/abc");
        test_normalize("../.././../abc/..", "../../..");
        test_normalize("/", "/");
    }

    private void test_normalize(String input, String expected) {
        String actual = PathExtension.normalize(input);
        assertEquals(expected, actual, String.format("E: input: `%s`", input));
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
