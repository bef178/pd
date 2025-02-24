package pd.util;

import java.util.AbstractMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_PropertiesCodec {

    @Test
    public void testEncode() {
        PropertiesCodec codec = new PropertiesCodec();

        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("a", "b");
        assertEquals("a=b", codec.encodeEntry(entry));
    }

    @Test
    public void testDecode() {
        PropertiesCodec codec = new PropertiesCodec();

        String s = "a=b";
        Map.Entry<String, String> entry = codec.decodeEntry(s);
        assertEquals("a", entry.getKey());
        assertEquals("b", entry.getValue());

        String s1 = "a1 =b1";
        Map.Entry<String, String> entry1 = codec.decodeEntry(s1);
        assertEquals("a1", entry1.getKey());
        assertEquals("b1", entry1.getValue());

        String s2 = "a-2 =  b-2  ";
        Map.Entry<String, String> entry2 = codec.decodeEntry(s2);
        assertEquals("a-2", entry2.getKey());
        assertEquals("b-2", entry2.getValue());
    }
}
