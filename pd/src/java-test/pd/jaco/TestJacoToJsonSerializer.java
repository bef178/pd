package pd.jaco;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJacoToJsonSerializer {

    JacoMan jacoMan = new JacoMan();

    String json = "{\"a\":1,\"s\":\"xxx\",\"m\":{\"k\":\"v\"},\"f32\":32.32,\"pi\":3.141592653589793}";

    @Test
    public void testToJson() {
        Object jaco = new JacoMan().setWithPath(null, "a", 1);
        jacoMan.setWithPath(jaco, "s", "xxx");
        jacoMan.setWithPath(jaco, "m/k", "v");
        jacoMan.setWithPath(jaco, "f32", 32.32);
        jacoMan.setWithPath(jaco, "pi", 3.141592653589793); // double has 16 significant digits

        assertEquals(json, jacoMan.toJson(jaco));
    }
}
