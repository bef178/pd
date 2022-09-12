package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import pd.fenc.CharReader;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;

public class Test_NumberPicker {

    private ScalarPicker picker = new ScalarPicker();

    @Test
    public void test_parseInt() {
        String raw = "1.1e5";
        assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt(raw);
        });
    }

    @Test
    public void test_pickFloat32() {
        CharReader src = new CharReader("0");
        assertEquals(0f, picker.pickFloat32(src));

        src = new CharReader("0.0");
        assertEquals(0f, picker.pickFloat32(src), 0.000001f);
        assertEquals(3, src.position());

        src = new CharReader("1");
        assertEquals(1f, picker.pickFloat32(src), 0.000001f);
        assertEquals(1, src.position());

        src = new CharReader("-1");
        assertEquals(-1f, picker.pickFloat32(src), 0.000001f);
        assertEquals(2, src.position());

        src = new CharReader("1.01");
        assertEquals(1.01f, picker.pickFloat32(src), 0.000001f);
        assertEquals(4, src.position());

        src = new CharReader("-1.1");
        assertEquals(-1.1f, picker.pickFloat32(src), 0.000001f);
        assertEquals(4, src.position());
    }

    @Test
    public void test_pickFloat32_failed() {
        for (String raw : new String[] { "-0", "0.", ".1", "-0.0" }) {
            CharReader src = new CharReader(raw);
            assertThrows(ParsingException.class, () -> {
                picker.pickFloat32(src);
            }, String.format("case [%s] failed", raw));
        }
    }

    @Test
    public void test_pickInt32() {
        CharReader src = new CharReader("00");
        assertEquals(0, picker.pickInt32(src));
        assertEquals(1, src.position());

        src = new CharReader("1");
        assertEquals(1, picker.pickInt32(src));
        assertEquals(1, src.position());
    }
}
