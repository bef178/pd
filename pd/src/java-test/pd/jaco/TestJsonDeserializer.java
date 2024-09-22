package pd.jaco;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonDeserializer {

    Jaco jaco = new Jaco();
    JsonDeserializer deserializer = new JsonDeserializer();

    @Test
    public void test() {
        String s = "{\"a\":1,\"s\":\"xxx\",\"m\":{\"k\":\"v\"},\"f32\":32.32,\"pi\":3.141592653589793}";
        Object o = deserializer.jsonToJaco(s);

        assertEquals(1, jaco.getWithPath(o, "a", Integer.class));
        assertEquals("xxx", jaco.getWithPath(o, "s", Object.class));
        assertEquals("v", jaco.getWithPath(o, "m/k", Object.class));
        assertEquals(new Float(32.32), jaco.getWithPath(o, "f32", Float.class));
        assertEquals(3.141592653589793, jaco.getWithPath(o, "pi", Object.class));
    }
}
