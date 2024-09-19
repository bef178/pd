package pd.jaco;

import java.util.List;

import org.junit.jupiter.api.Test;
import pd.util.JacksonUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestJacoSetter {

    private final Jaco jaco = new Jaco();

    private final Object source = JacksonUtil.jacksonDeserialize("{\"a\":{\"b\":[\"c\",\"d\"],\"e\":{\"1\":\"f\",\"2\":3,\"4\":5.0}}}", Object.class);

    @Test
    public void testSetIntoArray() {
        Object o = jaco.setWithPath(null, "a/b/1", "1");
        assertTrue(jaco.getWithPath(o, "a/b", Object.class) instanceof List);
        assertEquals("1", jaco.getWithPath(o, "a/b/1", String.class));
    }

    @Test
    public void testSetIntoArray_exception() {
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> jaco.setWithPath(source, "a/b/1d", "1"));
        assertEquals("KeyNotIndex: `1d`", expectedException.getMessage());
    }

    @Test
    public void testSetIntoMap() {
        Object o = jaco.setWithPath(null, "a/e/1", "f1");
        assertEquals("f1", jaco.getWithPath(o, "a/e/1", String.class));
    }

    @Test
    public void testSetIntegerIntoMap() {
        Object o = jaco.setWithPath(null, "a/e/6", 6);
        assertEquals(new Integer(6), jaco.getWithPath(o, "a/e/6", Integer.class));
    }

    @Test
    public void testSetLongIntoMap() {
        Object o = jaco.setWithPath(null, "a/e/7", 7);
        assertEquals(new Long(7), jaco.getWithPath(o, "a/e/7", Long.class));
    }

    @Test
    public void testSetDoubleIntoMap() {
        Object o = jaco.setWithPath(null, "a/e/8", 3.0);
        Double value = jaco.getWithPath(o, "a/e/8", Double.class);
        assertEquals(new Double(3), value);
    }
}
