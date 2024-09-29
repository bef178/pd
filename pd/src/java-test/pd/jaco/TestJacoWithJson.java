package pd.jaco;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.jaco.TestJacoWithPath.build;

public class TestJacoWithJson {

    JacoMan jacoMan = new JacoMan();

    String json = "{\"a\":[4,2],\"m\":{\"1\":\"1\",\"k\":\"v\"},\"i\":32,\"i64\":64,\"f\":32.32,\"pi\":3.141592653589793}";

    @Test
    public void testToJson() {
        Object o = build();
        assertEquals(json, jacoMan.toJson(o));
    }

    @Test
    public void testFromJson() {
        Object o = jacoMan.fromJson(json);
        assertEquals(4L, jacoMan.getWithPath(o, "a/0"));
        assertEquals("v", jacoMan.getWithPath(o, "m/k"));
        assertEquals(Double.class, jacoMan.getWithPath(o, "f").getClass());
        assertEquals(32.32d, jacoMan.getWithPath(o, "f"));
        assertEquals(3.141592653589793, jacoMan.getWithPath(o, "pi"));
    }

    @Test
    public void testFromToJson() {
        String json = "\"9\\n3d$fs冬你我他\""; // 冬: 0x2F81A, 194586
        Object o = "9\n3d$fs冬你我他";
        assertEquals(json, jacoMan.toJson(o));
        assertEquals(o, jacoMan.fromJson(json));

        json = "{\"a\":1,\"b\":2}";
        {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("a", 1L);
            m.put("b", 2L);
            o = m;
        }
        assertEquals(json, jacoMan.toJson(o));
        assertEquals(o, jacoMan.fromJson(json));

        json = "[\"a\",\"b\"]";
        {
            List<Object> a = new LinkedList<>();
            a.add("a");
            a.add("b");
            o = a;
        }
        assertEquals(json, jacoMan.toJson(o));
        assertEquals(o, jacoMan.fromJson(json));
    }
}
