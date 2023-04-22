package pd.fenc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_ScalarPicker {

    private ScalarPicker picker = new ScalarPicker();

    @Test
    public void test_pickDottedIdentifier() {
        CharReader src = new CharReader("a.b.c=");
        assertEquals("a.b.c", picker.pickDottedIdentifier(src));
    }

    @Test
    public void test_pickString() {
        CharReader src = new CharReader("hel\\lo world");
        assertEquals("hel\\lo", picker.pickString(src, ' '));
        assertEquals(' ', src.next());
    }
}
