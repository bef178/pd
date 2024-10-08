package pd.path;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestPathPattern {

    @Test
    public void testMatches() {
        assertFalse(PathPattern.matches("a/b", "/a/b"));

        assertTrue(PathPattern.matches("/a/*", "/a/bc"));
        assertTrue(PathPattern.matches("/a/*c", "/a/bc"));
        assertTrue(PathPattern.matches("/a/*c*", "/a/bc"));
        assertTrue(PathPattern.matches("/a/b*", "/a/bc"));
        assertTrue(PathPattern.matches("/a/bc*", "/a/bc"));
        assertTrue(PathPattern.matches("/a/b*c", "/a/bc"));
        assertTrue(PathPattern.matches("/a/*bc", "/a/bc"));
        assertFalse(PathPattern.matches("/a/b*d", "/a/bc"));
        assertFalse(PathPattern.matches("/a/c*", "/a/bc"));

        assertTrue(PathPattern.matches("a/*/c/*", "a/bb/c/d"));
        assertFalse(PathPattern.matches("a/*/d", "a/bb/c/d"));

        assertTrue(PathPattern.matches("a/**", "a/b/c/d"));
        assertTrue(PathPattern.matches("a/**d", "a/b/c/d"));
        assertTrue(PathPattern.matches("a/**/d", "a/b/c/d"));
        assertTrue(PathPattern.matches("a/**b/c/d", "a/b/c/d"));
    }
}
