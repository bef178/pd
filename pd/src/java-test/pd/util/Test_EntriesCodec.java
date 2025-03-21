package pd.util;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class Test_EntriesCodec {

    @Test
    public void testEncode() {
        EntriesCodec codec = new EntriesCodec();

        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("a", "b");
        assertEquals("a=b", codec.encode(Collections.singletonList(entry)));
    }

    @Test
    public void testEncodeSpace() {
        EntriesCodec codec = new EntriesCodec(':', '\n', 0, 1);

        Map.Entry<String, String> entry = new AbstractMap.SimpleImmutableEntry<>("a", "b");
        assertEquals("a: b", codec.encode(Collections.singletonList(entry)));
    }

    @Test
    public void testDecode() {
        EntriesCodec codec = new EntriesCodec();

        String s = "a=b";
        List<Map.Entry<String, String>> entries = codec.decode(s);
        assertEquals(1, entries.size());
        assertEquals("a", entries.get(0).getKey());
        assertEquals("b", entries.get(0).getValue());

        String s1 = "a1 =b1";
        List<Map.Entry<String, String>> entries1 = codec.decode(s1);
        assertEquals("a1 ", entries1.get(0).getKey());
        assertEquals("b1", entries1.get(0).getValue());

        String s2 = "a-2 =  b-2  ";
        List<Map.Entry<String, String>> entries2 = codec.decode(s2);
        assertEquals("a-2 ", entries2.get(0).getKey());
        assertEquals("  b-2  ", entries2.get(0).getValue());
    }

    @Test
    public void testDecodeSpace() {
        EntriesCodec codec = new EntriesCodec(':', '\n', 0, 1);

        {
            String s = "a:b";
            List<Map.Entry<String, String>> entries = codec.decode(s);
            assertEquals("a", entries.get(0).getKey());
            assertEquals("b", entries.get(0).getValue());
        }

        {
            String s = "a1 :b1";
            List<Map.Entry<String, String>> entries1 = codec.decode(s);
            assertEquals(1, entries1.size());
            assertEquals("a1 ", entries1.get(0).getKey());
            assertEquals("b1", entries1.get(0).getValue());
        }

        {
            String s = "a1: b1\na2: b2";
            List<Map.Entry<String, String>> entries2 = codec.decode(s);
            assertEquals(2, entries2.size());
            assertEquals("a1", entries2.get(0).getKey());
            assertEquals(" b1", entries2.get(0).getValue());
            assertEquals("a2", entries2.get(1).getKey());
            assertEquals(" b2", entries2.get(1).getValue());
        }
    }

    @Test
    public void testDecodeNoValue() {
        EntriesCodec codec = new EntriesCodec('=', '\n', 0, 0);

        {
            String s = "a";
            List<Map.Entry<String, String>> entries = codec.decode(s);
            assertEquals("a", entries.get(0).getKey());
            assertNull(entries.get(0).getValue());
        }

        {
            String s = "a=";
            List<Map.Entry<String, String>> entries = codec.decode(s);
            assertEquals("a", entries.get(0).getKey());
            assertEquals("", entries.get(0).getValue());
        }

        {
            String s = "#a";
            List<Map.Entry<String, String>> entries = codec.decode(s);
            assertEquals("#a", entries.get(0).getKey());
            assertNull(entries.get(0).getValue());
        }
    }
}
