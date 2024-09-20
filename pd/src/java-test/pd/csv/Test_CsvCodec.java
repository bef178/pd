package pd.csv;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import pd.csv.CsvCodec;

public class Test_CsvCodec {

    @Test
    public void test_deserialize() {
        String csvText = "PartId,\"Quantity\",'Color',4297719,'Bla\\'ck'";
        List<String> l = CsvCodec.deserialize(csvText);
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
        String csvText = "SetNumber,\"b\"\"bb\",c\r\n";
        assertEquals(csvText, CsvCodec.serialize(Arrays.asList(fields)));
        assertArrayEquals(fields, CsvCodec.deserialize(csvText).toArray(new String[0]));
    }
}
