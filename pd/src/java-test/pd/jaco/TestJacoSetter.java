package pd.jaco;

import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestJacoSetter {

    private final JacoMan jacoMan = new JacoMan();
    private final String json = "{\"a\":{\"b\":[\"c\",\"d\"],\"e\":{\"1\":\"f\",\"2\":3,\"4\":5.0}}}";
    private final Object jaco = jacoMan.fromJson(json);

    @Test
    public void testSetIntoArray() {
        Object o = jacoMan.setWithPath(null, "a/b/1", "1");
        assertTrue(jacoMan.getWithPath(o, "a/b") instanceof List);
        assertEquals("1", jacoMan.getWithPath(o, "a/b/1"));
    }

    @Test
    public void testSetIntoArray_exception() {
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> jacoMan.setWithPath(jaco, "a/b/1d", "1"));
        assertEquals("KeyNotIndex: `1d`", expectedException.getMessage());
    }

    @Test
    public void testSetIntoMap() {
        Object o = jacoMan.setWithPath(null, "a/e/1", "f1");
        assertEquals("f1", jacoMan.getWithPath(o, "a/e/1"));
    }

    @Test
    public void testSetIntegerIntoMap() {
        Object o = jacoMan.setWithPath(null, "a/e/6", 6);
        assertEquals(6, jacoMan.getWithPath(o, "a/e/6"));
    }

    @Test
    public void testSetLongIntoMap() {
        Object o = jacoMan.setWithPath(null, "a/e/7", 7L);
        Object value = jacoMan.getWithPath(o, "a/e/7");
        assertEquals(7L, value);
    }

    @Test
    public void testSetDoubleIntoMap() {
        Object o = jacoMan.setWithPath(null, "a/e/8", 3.0);
        Object value = jacoMan.getWithPath(o, "a/e/8");
        assertEquals(3.0D, value);
    }
}
