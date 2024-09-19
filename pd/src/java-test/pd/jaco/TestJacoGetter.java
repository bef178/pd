package pd.jaco;

import org.junit.jupiter.api.Test;
import pd.util.JacksonUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestJacoGetter {

    private final Jaco jaco = new Jaco();

    private final Object source = JacksonUtil.jacksonDeserialize("{\"a\":{\"b\":[\"c\",\"d\"],\"e\":{\"1\":\"f\",\"2\":3,\"4\":5.0}}}", Object.class);

    @Test
    public void testGetFromArray() {
        assertEquals("d", jaco.getWithPath(source, "a/b/1", String.class));
    }

    @Test
    public void testGetFromArray2() {
        assertNull(jaco.getWithPath(source, "a/b/2", String.class));
    }

    @Test
    public void testGetFromArray_exception() {
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> jaco.getWithPath(source, "a/b/1d", String.class));
        assertEquals("KeyNotIndex: `1d`", expectedException.getMessage());
    }

    @Test
    public void testGetFromMap() {
        String value = jaco.getWithPath(source, "a/e/1", String.class);
        assertEquals("f", value);
    }

    @Test
    public void testGetIntegerFromMap() {
        Integer value = jaco.getWithPath(source, "a/e/2", Integer.class);
        assertEquals(new Integer(3), value);
    }

    @Test
    public void testGetLongFromMap() {
        Long value = jaco.getWithPath(source, "a/e/2", Long.class);
        assertEquals(new Long(3), value);
    }

    @Test
    public void testGetDoubleFromMap() {
        Double value = jaco.getWithPath(source, "a/e/4", Double.class);
        assertEquals(new Double(5), value);
    }
}
