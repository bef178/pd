package pd.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestObjectExtension {

    @Test
    public void testConvertDouble() {
        String s = "2.0";
        Object o = JacksonUtil.jacksonDeserialize(s, Object.class);
        Double value = ObjectExtension.convert(o, Double.class);
        assertEquals(new Double(2), value);
    }
}
