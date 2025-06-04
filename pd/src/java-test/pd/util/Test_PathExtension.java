package pd.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class Test_PathExtension {

    @Test
    public void test_basename() {
        test_basename("/usr/lib", "lib");
        test_basename("/usr/", "usr");
        test_basename("usr", "usr");
        test_basename("/", "/");
        test_basename(".", ".");
        test_basename("..", "..");

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
        test_dirname("/usr/lib", "/usr");
        test_dirname("/usr/", "/");
        test_dirname("usr", ".");
        test_dirname("/", "/");
        test_dirname(".", ".");
        test_dirname("..", ".");

        test_dirname("abc", ".");
        test_dirname("abc//", ".");
        test_dirname("abc/../def", "abc/..");
        test_dirname("abc//.///", "abc");
    }

    private void test_dirname(String input, String expected) {
        String actual = PathExtension.dirname(input);
        assertEquals(expected, actual, String.format("E: input: `%s`", input));
    }

    @Test
    public void test_extname() {
        test_extname("index.html", ".html");
        test_extname("index.coffee.md", ".md");
        test_extname("index.", ".");
        test_extname("index", "");
        test_extname(".index", "");
        test_extname(".index.md", ".md");

        test_extname("/tmp/a.b.c", ".c");
        test_extname("/tmp/a", "");
    }

    private void test_extname(String input, String expected) {
        String actual = PathExtension.extname(input);
        assertEquals(expected, actual, String.format("E: input: `%s`", input));
    }

    @Test
    public void test_join() {
        assertEquals("abc//def/ggg", PathExtension.join("abc", "/def", "ggg"));
    }

    @Test
    public void test_normalize() {
        test_normalize("abc", "./abc");
        test_normalize("././abc", "./abc");
        test_normalize("./../abc", "../abc");
        test_normalize("/../abc", "/abc");
        test_normalize("../.././../abc/..", "../../..");
        test_normalize("/", "/");
        test_normalize("//a", "/a");
        test_normalize("a///b", "./a/b");
        test_normalize("a/../../b", "../b");
    }

    private void test_normalize(String input, String expected) {
        String actual = PathExtension.normalize(input);
        assertEquals(expected, actual, String.format("E: input: `%s`", input));
    }

    @Test
    public void test_resolve() {
        assertEquals("//a", PathExtension.resolve("/", "a"));
        assertEquals("a/b", PathExtension.resolve("a", "b"));
        assertEquals("/b", PathExtension.resolve("a", "/b"));
        assertEquals("a///b", PathExtension.resolve("a//", "b"));
        assertEquals("a//b/c", PathExtension.resolve("a/", "b", "c"));
        assertEquals("//c", PathExtension.resolve("a/", "b", "//c"));
    }

    @Test
    public void test_compare() {
        assertEquals(0, PathExtension.compare("abc", "abc"));
        assertEquals(-1, PathExtension.compare("abc", "bc"));
        assertEquals(1, PathExtension.compare("bc", "abc"));
        assertEquals(-1, PathExtension.compare("a", "abc"));
        assertEquals(1, PathExtension.compare("abc", "a"));
        assertEquals(-1, PathExtension.compare("abc/", "a"));
        assertEquals(1, PathExtension.compare("a", "abc/"));
        assertEquals(-1, PathExtension.compare("a", "abc/a"));
        assertEquals(1, PathExtension.compare("abc/a", "a"));
        assertEquals(-1, PathExtension.compare("abc.txt", "abc (abc).txt"));
        assertEquals(1, PathExtension.compare("abc (copy).txt", "abc.txt"));
    }
}
