package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import pd.util.CurvePattern;

public class Test_CurvePattern {

    @Test
    public void test_match() {
        String pattern = "a/{cusId}/b({accId})/c{camId}";
        String s = "a/1/b(2)/c3";
        Map<String, String> capturingGroups = CurvePattern.match(pattern, s);
        assertEquals(3, capturingGroups.size());
        assertEquals("1", capturingGroups.get("cusId"));
        assertEquals("2", capturingGroups.get("accId"));
        assertEquals("3", capturingGroups.get("camId"));
    }
}
