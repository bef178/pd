package pd.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_RegexExtension {

    @Test
    public void test_match() {
        List<String> groups = RegexExtension.match("^[a-zA-Z0-9_-]+ ([a-zA-Z0-9_-]+)", "abc 123\ndef 456");
        assertEquals(4, groups.size());
        assertEquals("123", groups.get(1));
        assertEquals("456", groups.get(3));
    }
}
