package pd.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test_PathPattern {

    PathPattern pathPattern = PathPattern.singleton();

    @Test
    public void test() {
        assertTrue(pathPattern.matches("/a/*", "/a/bc"));
        assertTrue(pathPattern.matches("/a/b*", "/a/bc"));
        assertTrue(pathPattern.matches("/a/b*c", "/a/bc"));

        assertFalse(pathPattern.matches("/a/b*", "/a/c"));
        assertFalse(pathPattern.matches("/a/b*d", "/a/bc"));

        assertTrue(pathPattern.matches("a/*c", "a/c"));

        assertFalse(pathPattern.matches("a/*c", "/a/c"));
    }
}
