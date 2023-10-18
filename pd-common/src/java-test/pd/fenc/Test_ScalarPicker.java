package pd.fenc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_ScalarPicker {

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    @Test
    public void test_pickDottedIdentifier() {
        Int32Feeder src = new Int32Feeder("a.b.c=");
        assertEquals("a.b.c", scalarPicker.pickDottedIdentifier(src));
    }

    @Test
    public void test_pickString() {
        Int32Feeder src = new Int32Feeder("hel\\lo world");
        assertEquals("hel\\lo", scalarPicker.pickString(src, ' '));
        assertEquals(' ', src.next());
    }
}
