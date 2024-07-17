package pd.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pd.util.JcoExtension.JcoException;

public class TestJcoGetter {

    @Test
    public void testGetFromArray() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        String value = JcoExtension.get(o, "a/b/1", String.class);
        assertEquals("d", value);
    }

    @Test
    public void testGetFromArray_null() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        String value = JcoExtension.getOrNull(o, "a/b/1d", String.class);
        assertNull(value);
    }

    @Test
    public void testGetFromArray_exception() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Exception expectedException = assertThrows(
                JcoException.class,
                () -> JcoExtension.get(o, "a/b/1d", String.class));
        assertEquals("KeyNotFound: `1d` of `ArrayList`", expectedException.getMessage());
    }

    @Test
    public void testGetFromArray_exception2() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Exception expectedException = assertThrows(
                JcoException.class,
                () -> JcoExtension.get(o, "a/b/2", String.class));
        assertEquals("KeyNotFound: `2` of `ArrayList`", expectedException.getMessage());
    }

    @Test
    public void testGetFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":\"d\"}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        String value = JcoExtension.get(o, "a/b/1", String.class);
        assertEquals("d", value);
    }

    @Test
    public void testGetIntegerFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":2}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Integer value = JcoExtension.get(o, "a/b/1", Integer.class);
        assertEquals(new Integer(2), value);
    }

    @Test
    public void testGetLongFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":2}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Long value = JcoExtension.get(o, "a/b/1", Long.class);
        assertEquals(new Long(2), value);
    }

    @Test
    public void testGetDoubleFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":2.0}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Double value = JcoExtension.get(o, "a/b/1", Double.class);
        assertEquals(new Double(2), value);
    }

    @Test
    public void testConvertDouble() {
        String s = "2.0";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Double value = JcoExtension.convert(o, Double.class);
        assertEquals(new Double(2), value);
    }
}
