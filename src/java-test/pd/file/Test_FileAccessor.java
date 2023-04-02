package pd.file;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Test_FileAccessor {

    @Test
    public void test_listDirectory() {
        List<String> paths = FileAccessor.singleton().listDirectory(".");
        assertTrue(paths != null);
        assertTrue(paths.contains("Makefile"));
        assertTrue(paths.contains("src"));

        paths = FileAccessor.singleton().listDirectory("./Makefile");
        assertNull(paths);
    }

    @Test
    public void test_listDirectory2() {
        List<String> paths = FileAccessor.singleton().listDirectory2(".");
        assertTrue(paths != null);
        assertTrue(paths.contains("./Makefile"));
        assertTrue(paths.contains("./src"));

        paths = FileAccessor.singleton().listDirectory2("./Makefile");
        assertNull(paths);
    }
}
