package pd.jaco;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestJacoGetter {

    private final JacoMan jacoMan = new JacoMan();
    private final String json = "{\"a\":{\"b\":[\"c\",\"d\"],\"e\":{\"1\":\"f\",\"2\":3,\"4\":5.0}}}";
    private final Object jaco = jacoMan.fromJson(json);

    @Test
    public void testGetFromArray() {
        assertEquals("d", jacoMan.getWithPath(jaco, "a/b/1"));
    }

    @Test
    public void testGetFromArray2() {
        assertNull(jacoMan.getWithPath(jaco, "a/b/2"));
    }

    @Test
    public void testGetFromArray_exception() {
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> jacoMan.getWithPath(jaco, "a/b/1d"));
        assertEquals("KeyNotIndex: `1d`", expectedException.getMessage());
    }

    @Test
    public void testGetFromMap() {
        Object value = jacoMan.getWithPath(jaco, "a/e/1");
        assertEquals("f", value);
    }

    @Test
    public void testGetIntegerFromMap() {
        Object value = jacoMan.getWithPath(jaco, "a/e/2");
        assertEquals(3L, value);
    }

    @Test
    public void testGetDoubleFromMap() {
        Object value = jacoMan.getWithPath(jaco, "a/e/4");
        assertEquals(5.0D, value);
    }
}
