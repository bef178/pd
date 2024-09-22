package pd.jaco;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestJsonSerializer {

    Jaco jaco = new Jaco();
    JsonSerializer serializer = new JsonSerializer();

    @Test
    public void test() {
        Object o = new Jaco().setWithPath(null, "a", 1);
        jaco.setWithPath(o, "s", "xxx");
        jaco.setWithPath(o, "m/k", "v");
        jaco.setWithPath(o, "f32", 32.32);
        jaco.setWithPath(o, "pi", 3.141592653589793); // double has 16 significant digits
        String actual = serializer.jacoToJson(o);
        assertEquals("{\"a\":1,\"s\":\"xxx\",\"m\":{\"k\":\"v\"},\"f32\":32.32,\"pi\":3.141592653589793}", actual);
    }
}
