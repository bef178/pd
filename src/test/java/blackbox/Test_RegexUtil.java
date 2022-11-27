package blackbox;

import org.junit.jupiter.api.Test;
import pd.util.RegexUtil;

import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_RegexUtil {

    @Test
    public void test_match() {
        List<String> groups = RegexUtil.match("^[a-zA-Z0-9_-]+ ([a-zA-Z0-9_-]+)", "abc 123\ndef 456");
        assertEquals(4, groups.size());
        assertEquals("123", groups.get(1));
        assertEquals("456", groups.get(3));
    }
}
