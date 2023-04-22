package pd.file;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test_LocalFileAccessor {

    private static final LocalFileAccessor accessor = LocalFileAccessor.singleton();

    @Test
    public void test_list2() {
        List<String> paths = accessor.list2("");
        assertNotNull(paths);
        assertTrue(paths.contains("pom.xml"));
        assertTrue(paths.contains("src/"));
    }

    @Test
    public void test_listDirectory() {
        List<String> paths = accessor.listDirectory(".");
        assertNotNull(paths);
        assertTrue(paths.contains("pom.xml"));
        assertTrue(paths.contains("src"));

        paths = accessor.listDirectory("./pom.xml");
        assertNull(paths);
    }

    @Test
    public void test_listDirectory2() {
        List<String> paths = accessor.listDirectory2(".");
        assertNotNull(paths);
        assertTrue(paths.contains("./pom.xml"));
        assertTrue(paths.contains("./src"));

        paths = accessor.listDirectory2("./pom.xml");
        assertNull(paths);
    }
}
