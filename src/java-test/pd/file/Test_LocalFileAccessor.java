package pd.file;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test_LocalFileAccessor {

    private static final LocalFileAccessor fileAccessor = LocalFileAccessor.singleton();

    @Test
    public void test_list2() {
        List<String> paths = fileAccessor.list2("");
        assertNotNull(paths);
        assertTrue(paths.contains("Makefile"));
        assertTrue(paths.contains("src/"));
    }

    @Test
    public void test_listDirectory() {
        List<String> paths = fileAccessor.listDirectory(".");
        assertNotNull(paths);
        assertTrue(paths.contains("Makefile"));
        assertTrue(paths.contains("src"));

        paths = fileAccessor.listDirectory("./Makefile");
        assertNull(paths);
    }

    @Test
    public void test_listDirectory2() {
        List<String> paths = fileAccessor.listDirectory2(".");
        assertNotNull(paths);
        assertTrue(paths.contains("./Makefile"));
        assertTrue(paths.contains("./src"));

        paths = fileAccessor.listDirectory2("./Makefile");
        assertNull(paths);
    }
}
