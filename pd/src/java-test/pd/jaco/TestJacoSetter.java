package pd.jaco;

import org.junit.jupiter.api.Test;
import pd.util.JacksonUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestJacoSetter {

    @Test
    public void testSetIntoArray() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        JacoExtension.setWithPath(o, "a/b/1", "1");
        assertEquals("1", JacoExtension.getWithPath(o, "a/b/1", String.class));
    }

    @Test
    public void testSetIntoArray_null() {
        String s = "{\"a\":{}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        JacoExtension.setWithPath(o, "a/b/1", "1");
        assertEquals("1", JacoExtension.getWithPath(o, "a/b/1", String.class));
    }

    @Test
    public void testSetIntoArray_exception() {
        String s = "{\"a\":{\"b\":[\"c\",\"d\"]}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Exception expectedException = assertThrows(
                JacoException.class,
                () -> JacoExtension.setWithPath(o, "a/b/1d", "1"));
        assertEquals("KeyNotFound: `1d` of `ArrayList`", expectedException.getMessage());
    }

    @Test
    public void testSetIntoMap() {
        String s = "{\"a\":{\"b\":{\"1\":\"d\"}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        JacoExtension.setWithPath(o, "a/b/1", "2");
        assertEquals("2", JacoExtension.getWithPath(o, "a/b/1", String.class));
    }

    @Test
    public void testSetIntegerIntoMap() {
        String s = "{\"a\":{\"b\":{\"1\":2}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        JacoExtension.setWithPath(o, "a/b/1", 1);
        assertEquals(new Integer(1), JacoExtension.getWithPath(o, "a/b/1", Integer.class));
    }

    @Test
    public void testSetLongIntoMap() {
        String s = "{\"a\":{\"b\":{\"1\":2}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        JacoExtension.setWithPath(o, "a/b/1", 1);
        assertEquals(new Long(1), JacoExtension.getWithPath(o, "a/b/1", Long.class));
    }

    @Test
    public void testSetDoubleIntoMap() {
        String s = "{\"a\":{\"b\":{\"1\":2.0}}}";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        JacoExtension.setWithPath(o, "a/b/1", 3.0);
        Double value = JacoExtension.getWithPath(o, "a/b/1", Double.class);
        assertEquals(new Double(3), value);
    }
}
