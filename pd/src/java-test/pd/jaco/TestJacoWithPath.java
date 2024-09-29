package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestJacoWithPath {

    private final JacoMan jacoMan = new JacoMan();

    static Object build() {
        Map<String, Object> o = new LinkedHashMap<>();
        {
            List<Object> a = new LinkedList<>();
            a.add(4);
            a.add(2);
            o.put("a", a);
        }
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("1", "1");
            m.put("k", "v");
            o.put("m", m);
        }
        o.put("i", 32);
        o.put("i64", 64L);
        o.put("f", 32.32f);
        o.put("pi", 3.141592653589793); // double has 16 significant digits
        return o;
    }

    @Test
    public void testGetFromArray() {
        Object o = build();
        assertEquals(2, jacoMan.getWithPath(o, "a/1"));
        assertNull(jacoMan.getWithPath(o, "a/2"));
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> jacoMan.getWithPath(o, "a/1d"));
        assertEquals("KeyNotIndex: `1d`", expectedException.getMessage());
    }

    @Test
    public void testGetFromMap() {
        Object o = build();
        assertEquals("1", jacoMan.getWithPath(o, "m/1"));
        assertEquals("v", jacoMan.getWithPath(o, "m/k"));
        assertEquals(32, jacoMan.getWithPath(o, "i"));
        assertEquals(32.32f, jacoMan.getWithPath(o, "f"));
        assertEquals(3.141592653589793, jacoMan.getWithPath(o, "pi"));
    }

    @Test
    public void testSetIntoArray() {
        Object o = jacoMan.setWithPath(null, "m/a/1", "1");
        assertTrue(jacoMan.getWithPath(o, "m/a") instanceof List);
        assertEquals("1", jacoMan.getWithPath(o, "m/a/1"));
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> jacoMan.setWithPath(o, "m/a/1d", "1"));
        assertEquals("KeyNotIndex: `1d`", expectedException.getMessage());
    }

    @Test
    public void testSetIntoMap() {
        Object o = jacoMan.setWithPath(null, "a/e/1", "f1");
        assertEquals("f1", jacoMan.getWithPath(o, "a/e/1"));

        jacoMan.setWithPath(o, "a/e/6", 6);
        assertEquals(6, jacoMan.getWithPath(o, "a/e/6"));

        jacoMan.setWithPath(o, "a/e/7", 7L);
        assertEquals(7L, jacoMan.getWithPath(o, "a/e/7"));

        jacoMan.setWithPath(o, "a/e/8", 8.0);
        assertEquals(8.0, jacoMan.getWithPath(o, "a/e/8"));
    }
}
