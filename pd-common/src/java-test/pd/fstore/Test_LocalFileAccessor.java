package pd.fstore;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test_LocalFileAccessor {

    private static final LocalFileAccessor accessor = LocalFileAccessor.singleton();

    @Test
    public void test_list() {
        List<String> paths = accessor.list("");
        assertTrue(paths.contains("pom.xml"));
        assertTrue(paths.contains("src/"));
    }

    @Test
    public void test_list_2() {
        List<String> paths = accessor.list("src");
        assertTrue(paths.contains("src/"));
    }

    @Test
    public void test_list_3() {
        List<String> paths = accessor.list("src/j");
        assertTrue(paths.contains("src/java/"));
    }

    @Test
    public void test_listAll() {
        List<String> paths = accessor.listAll("src");
        assertTrue(paths.contains("src/java/pd/file/LocalFileAccessor.java"));
    }
}
