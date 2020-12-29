package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import pd.fenc.IReader;
import pd.fenc.Int32Scanner;
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
        Int32Scanner it = new Int32Scanner(IReader.wrap("a.b.c="));
        assertEquals("a.b.c", ScalarPicker.pickDottedIdentifier(it));
    }

    @Test
    public void test_pickFloat32() {
        Int32Scanner it = new Int32Scanner(IReader.wrap("0"));
        assertEquals(0f, ScalarPicker.pickFloat32(it));

        it = new Int32Scanner(IReader.wrap("0.0"));
        assertEquals(0f, ScalarPicker.pickFloat32(it), 0.000001f);
        assertEquals(3, it.position());

        it = new Int32Scanner(IReader.wrap("1"));
        assertEquals(1f, ScalarPicker.pickFloat32(it), 0.000001f);

        it = new Int32Scanner(IReader.wrap("-1"));
        assertEquals(-1f, ScalarPicker.pickFloat32(it), 0.000001f);

        it = new Int32Scanner(IReader.wrap("1.01"));
        assertEquals(1.01f, ScalarPicker.pickFloat32(it), 0.000001f);

        it = new Int32Scanner(IReader.wrap("-1.1"));
        assertEquals(-1.1f, ScalarPicker.pickFloat32(it), 0.000001f);
    }

    @Test
    public void test_pickFloat32_failed() {
        for (String raw : new String[] {
                "-0", "0.", ".1", "-0.0"
        }) {
            Int32Scanner it = new Int32Scanner(IReader.wrap(raw));
            assertThrows(ParsingException.class, () -> {
                ScalarPicker.pickFloat32(it);
            }, String.format("case [%s] failed", raw));
        }
    }

    @Test
    public void test_pickInt32() {
        Int32Scanner it = new Int32Scanner(IReader.wrap("00"));
        assertEquals(0, ScalarPicker.pickInt32(it));
        assertEquals(1, it.position());
    }
}
