package pd.fenc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class Test_NumberPicker {

    private NumberPicker picker = NumberPicker.singleton();

    @Test
    public void test_parseInt() {
        String raw = "1.1e5";
        assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt(raw);
        });
    }

    @Test
    public void test_pickFloat32() {
        BackableUnicodeProvider src = new BackableUnicodeProvider("0");
        assertEquals(0f, picker.pickFloat32(src));

        src = new BackableUnicodeProvider("0.0");
        assertEquals(0f, picker.pickFloat32(src), 0.000001f);
        assertEquals(3, src.position());

        src = new BackableUnicodeProvider("1");
        assertEquals(1f, picker.pickFloat32(src), 0.000001f);
        assertEquals(1, src.position());

        src = new BackableUnicodeProvider("-1");
        assertEquals(-1f, picker.pickFloat32(src), 0.000001f);
        assertEquals(2, src.position());

        src = new BackableUnicodeProvider("1.01");
        assertEquals(1.01f, picker.pickFloat32(src), 0.000001f);
        assertEquals(4, src.position());

        src = new BackableUnicodeProvider("-1.1");
        assertEquals(-1.1f, picker.pickFloat32(src), 0.000001f);
        assertEquals(4, src.position());
    }

    @Test
    public void test_pickFloat32_failed() {
        for (String raw : new String[] { "-0", "0.", ".1", "-0.0" }) {
            BackableUnicodeProvider src = new BackableUnicodeProvider(raw);
            assertThrows(ParsingException.class, () -> {
                picker.pickFloat32(src);
            }, String.format("case [%s] failed", raw));
        }
    }

    @Test
    public void test_pickInt32() {
        BackableUnicodeProvider src = new BackableUnicodeProvider("00");
        assertEquals(0, picker.pickInt32(src));
        assertEquals(1, src.position());

        src = new BackableUnicodeProvider("1");
        assertEquals(1, picker.pickInt32(src));
        assertEquals(1, src.position());
    }
}
