package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestJacoMan {

    private final JacoMan jacoMan = new JacoMan();

    @Test
    public void testGet() {
        final Object jaco;
        {
            Map<String, Object> o = new LinkedHashMap<>();
            {
                List<Object> a = new LinkedList<>();
                a.add(4L);
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
            jaco = o;
        }

        assertEquals(4L, jacoMan.getWithPath(jaco, "a/0"));
        assertEquals(2, jacoMan.getWithPath(jaco, "a/1"));
        assertNull(jacoMan.getWithPath(jaco, "a/2"));
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> jacoMan.getWithPath(jaco, "a/1d"));
        assertEquals("KeyNotIndex: `1d`", expectedException.getMessage());

        assertEquals("1", jacoMan.getWithPath(jaco, "m/1"));
        assertEquals("v", jacoMan.getWithPath(jaco, "m/k"));
        assertEquals(32, jacoMan.getWithPath(jaco, "i"));
        assertEquals(32.32f, jacoMan.getWithPath(jaco, "f"));
        assertEquals(3.141592653589793, jacoMan.getWithPath(jaco, "pi"));
    }

    @Test
    public void testSetIntoArray() {
        Object o = jacoMan.setWithPath(null, "m/a/1", "1");
        assertInstanceOf(List.class, jacoMan.getWithPath(o, "m/a"));
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

    @Test
    public void testRemove() {
        Map<String, Object> o = new LinkedHashMap<>();
        {
            LinkedHashMap<String, Object> m = new LinkedHashMap<>();
            m.put("k", "v");
            o.put("m", m);
        }
        jacoMan.removeWithPath(o, "m/k");
        assertNull(jacoMan.getWithPath(o, "m/k"));
        assertNotNull(jacoMan.getWithPath(o,"m"));
    }

    @Test
    public void testFlatten() {
        Map<String[], Object> pairs = jacoMan.flatten(null);
        assertEquals(1, pairs.size());
        assertEquals(0, pairs.keySet().iterator().next().length);
        assertNull(pairs.values().iterator().next());
    }
}
