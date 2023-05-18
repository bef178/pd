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
        assertTrue(paths.contains("pom.xml"));
        assertTrue(paths.contains("src/"));
    }

    @Test
    public void test_list2_2() {
        List<String> paths = accessor.list2("src");
        assertTrue(paths.contains("src/"));
    }

    @Test
    public void test_list2_3() {
        List<String> paths = accessor.list2("src/j");
        assertTrue(paths.contains("src/java/"));
    }

    @Test
    public void test_listAllRegularFiles() {
        List<String> paths = accessor.listAllRegularFiles("src");
        assertTrue(paths.contains("src/java/pd/file/LocalFileAccessor.java"));
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

        paths = accessor.listDirectory2("");
        assertNotNull(paths);
        assertTrue(paths.contains("pom.xml"));
        assertTrue(paths.contains("src"));

        paths = accessor.listDirectory2("pom.xml");
        assertNull(paths);
    }

    @Test
    public void test_listRegularFiles() {
        List<String> paths = accessor.listRegularFiles("", 2);
        assertTrue(paths.contains("pom.xml"));
        assertTrue(paths.contains("out/pd-common-1.0-SNAPSHOT.jar"));
    }
}
