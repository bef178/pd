package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import pd.fenc.CharReader;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;

public class Test_ScalarPicker {

    @Test
    public void test_parseInt() {
        String raw = "1.1e5";
        assertThrows(NumberFormatException.class, () -> {
            Integer.parseInt(raw);
        });
    }

    @Test
    public void test_pickDottedIdentifier() {
        CharReader it = new CharReader("a.b.c=");
        assertEquals("a.b.c", ScalarPicker.pickDottedIdentifier(it));
    }

    @Test
    public void test_pickFloat32() {
        CharReader it = new CharReader("0");
        assertEquals(0f, ScalarPicker.pickFloat32(it));

        it = new CharReader("0.0");
        assertEquals(0f, ScalarPicker.pickFloat32(it), 0.000001f);
        assertEquals(3, it.position());

        it = new CharReader("1");
        assertEquals(1f, ScalarPicker.pickFloat32(it), 0.000001f);

        it = new CharReader("-1");
        assertEquals(-1f, ScalarPicker.pickFloat32(it), 0.000001f);

        it = new CharReader("1.01");
        assertEquals(1.01f, ScalarPicker.pickFloat32(it), 0.000001f);

        it = new CharReader("-1.1");
        assertEquals(-1.1f, ScalarPicker.pickFloat32(it), 0.000001f);
    }

    @Test
    public void test_pickFloat32_failed() {
        for (String raw : new String[] {
                "-0", "0.", ".1", "-0.0"
        }) {
            CharReader it = new CharReader(raw);
            assertThrows(ParsingException.class, () -> {
                ScalarPicker.pickFloat32(it);
            }, String.format("case [%s] failed", raw));
        }
    }

    @Test
    public void test_pickInt32() {
        CharReader it = new CharReader("00");
        assertEquals(0, ScalarPicker.pickInt32(it));
        assertEquals(1, it.position());
    }

    @Test
    public void test_pickString() {
        CharReader it = new CharReader("hel\\lo world");
        assertEquals("hel\\lo", ScalarPicker.pickString(it, ' '));
        assertEquals(' ', it.next());
    }
}
