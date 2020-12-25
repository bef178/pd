package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pd.fenc.IReader;
import pd.fenc.Int32Scanner;
import pd.fenc.ParsingException;
import pd.fenc.ScalarPicker;

public class Test_ScalarPicker {

    @Test
    public void test_pickDottedIdentifier() {
        Int32Scanner it = new Int32Scanner(IReader.wrap("a.b.c="));
        assertEquals("a.b.c", ScalarPicker.pickDottedIdentifier(it));
    }

    @Test
    public void test_pickFloat() {
        Int32Scanner it = new Int32Scanner(IReader.wrap("0"));
        assertEquals(0f, ScalarPicker.pickFloat(it), 0.000001f);

        it = new Int32Scanner(IReader.wrap("1"));
        assertEquals(1f, ScalarPicker.pickFloat(it), 0.000001f);

        it = new Int32Scanner(IReader.wrap("-1"));
        assertEquals(-1f, ScalarPicker.pickFloat(it), 0.000001f);

        it = new Int32Scanner(IReader.wrap("1.01"));
        assertEquals(1.01f, ScalarPicker.pickFloat(it), 0.000001f);

        it = new Int32Scanner(IReader.wrap("-1.1"));
        assertEquals(-1.1f, ScalarPicker.pickFloat(it), 0.000001f);
    }

    @Test
    public void test_pickFloat_failed() {
        boolean ok = true;
        Int32Scanner it = new Int32Scanner(IReader.wrap("00"));
        try {
            ScalarPicker.pickFloat(it);
        } catch (ParsingException e) {
            ok = false;
        }
        assertEquals(false, ok);

        ok = true;
        it = new Int32Scanner(IReader.wrap("-0"));
        try {
            ScalarPicker.pickFloat(it);
        } catch (ParsingException e) {
            ok = false;
        }
        assertEquals(false, ok);

        ok = true;
        it = new Int32Scanner(IReader.wrap("0."));
        try {
            ScalarPicker.pickFloat(it);
        } catch (ParsingException e) {
            ok = false;
        }
        assertEquals(false, ok);

        ok = true;
        it = new Int32Scanner(IReader.wrap(".1"));
        try {
            ScalarPicker.pickFloat(it);
        } catch (ParsingException e) {
            ok = false;
        }
        assertEquals(false, ok);

        ok = true;
        it = new Int32Scanner(IReader.wrap("-0.0"));
        try {
            ScalarPicker.pickFloat(it);
        } catch (ParsingException e) {
            ok = false;
        }
        assertEquals(false, ok);

        ok = true;
        it = new Int32Scanner(IReader.wrap("0.0"));
        try {
            ScalarPicker.pickFloat(it);
        } catch (ParsingException e) {
            ok = false;
        }
        assertEquals(false, ok);
    }
}
