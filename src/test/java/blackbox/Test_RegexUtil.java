package blackbox;

import org.junit.jupiter.api.Test;
import pd.util.RegexUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_RegexUtil {

    @Test
    public void test_match() {
        List<String> groups = RegexUtil.match("^([a-zA-Z0-9_-]+) ([a-zA-Z0-9_-]+)", "abc 123");
        assertEquals(3, groups.size());
        assertEquals("abc", groups.get(1));
        assertEquals("123", groups.get(2));
    }
}
