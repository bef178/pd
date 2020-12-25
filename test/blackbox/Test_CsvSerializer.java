package blackbox;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import pd.fenc.CsvSerializer;

public class Test_CsvSerializer {

    @Test
    public void test_deserialize() {
        String csv = "PartId,\"Quantity\",'Color',4297719,'Bla\\'ck'";
        List<String> l = CsvSerializer.deserializeRecord(csv);
        assertEquals("PartId", l.get(0));
        assertEquals("Quantity", l.get(1));
        assertEquals("'Color'", l.get(2));
        assertEquals("4297719", l.get(3));
        assertEquals("'Bla\\'ck'", l.get(4));
    }

    @Test
    public void test_serialize() {
        String[] fields = new String[] {
                "SetNumber", "b\"bb", "c"
        };
        String csv = "SetNumber,\"b\"\"bb\",c\r\n";
        assertEquals(csv, CsvSerializer.serializeRecord(Arrays.asList(fields), ','));
        assertArrayEquals(fields, CsvSerializer.deserializeRecord(csv).toArray(new String[0]));
    }
}
