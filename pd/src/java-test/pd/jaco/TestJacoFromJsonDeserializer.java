package pd.jaco;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJacoFromJsonDeserializer {

    JacoMan jacoMan = new JacoMan();

    String json = "{\"a\":1,\"s\":\"xxx\",\"m\":{\"k\":\"v\"},\"f32\":32.32,\"pi\":3.141592653589793}";

    @Test
    public void testFromJson() {
        Object jaco = jacoMan.fromJson(json);

        assertEquals(1L, jacoMan.getWithPath(jaco, "a"));
        assertEquals("xxx", jacoMan.getWithPath(jaco, "s"));
        assertEquals("v", jacoMan.getWithPath(jaco, "m/k"));
        assertEquals(32.32D, jacoMan.getWithPath(jaco, "f32"));
        assertEquals(3.141592653589793, jacoMan.getWithPath(jaco, "pi"));
    }
}
