package pd.util;

import org.junit.jupiter.api.Test;
import pd.jaco.JsonMan;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_ObjectExtension {

    @Test
    public void testConvertDouble() {
        String s = "2.0";
        Object o = new JsonMan().deserialize(s, Object.class, ".");
        Double value = ObjectExtension.convert(o, Double.class);
        assertEquals(new Double(2), value);
    }
}
