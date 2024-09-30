package pd.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

public class Test_CurlyBracketPatternExtension {

    @Test
    public void test_format() {
        String pattern = "a/{}/b({})/c{}";
        assertEquals("a/1/b(2)/c3", CurlyBracketPatternExtension.format(pattern, 1, 2, 3));
    }

    @Test
    public void test_match() {
        String pattern = "a/{cusId}/b({accId})/c{camId}";
        String s = "a/1/b(2)/c3";
        Map<String, String> capturingGroups = CurlyBracketPatternExtension.match(pattern, s);
        assertEquals(3, capturingGroups.size());
        assertEquals("1", capturingGroups.get("cusId"));
        assertEquals("2", capturingGroups.get("accId"));
        assertEquals("3", capturingGroups.get("camId"));
    }

    @Test
    public void test_match_2() {
        String pattern = "a{x}b{y}c{z}c";
        String s = "aaabccccc";
        Map<String, String> capturingGroups = CurlyBracketPatternExtension.match(pattern, s);
        assertEquals(3, capturingGroups.size());
        assertEquals("aa", capturingGroups.get("x"));
        assertEquals("", capturingGroups.get("y"));
        assertEquals("ccc", capturingGroups.get("z"));
    }
}
