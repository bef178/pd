package pd.jaco;

import org.junit.jupiter.api.Test;
import pd.util.JacksonUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestJacoGetter {

    @Test
    public void testGetFromArray() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        String value = JacoExtension.getWithPath(o, "a/b/1", String.class);
        assertEquals("d", value);
    }

    @Test
    public void testGetFromArray_null() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        String value = JacoExtension.getOrNullWithPath(o, "a/b/1d", String.class);
        assertNull(value);
    }

    @Test
    public void testGetFromArray_exception() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> JacoExtension.getWithPath(o, "a/b/1d", String.class));
        assertEquals("KeyNotFound: `1d` of `ArrayList`", expectedException.getMessage());
    }

    @Test
    public void testGetFromArray_exception2() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> JacoExtension.getWithPath(o, "a/b/2", String.class));
        assertEquals("KeyNotFound: `2` of `ArrayList`", expectedException.getMessage());
    }

    @Test
    public void testGetFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":\"d\"}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        String value = JacoExtension.getWithPath(o, "a/b/1", String.class);
        assertEquals("d", value);
    }

    @Test
    public void testGetIntegerFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":2}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Integer value = JacoExtension.getWithPath(o, "a/b/1", Integer.class);
        assertEquals(new Integer(2), value);
    }

    @Test
    public void testGetLongFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":2}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Long value = JacoExtension.getWithPath(o, "a/b/1", Long.class);
        assertEquals(new Long(2), value);
    }

    @Test
    public void testGetDoubleFromMap() {
        String s = "{\"a\":{\"b\":{\"1\":2.0}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Double value = JacoExtension.getWithPath(o, "a/b/1", Double.class);
        assertEquals(new Double(2), value);
    }

    @Test
    public void testConvertDouble() {
        String s = "2.0";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Double value = JacoExtension.convert(o, Double.class);
        assertEquals(new Double(2), value);
    }
}
