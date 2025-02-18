package pd.fenc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_ScalarPicker {

    private final ScalarPicker scalarPicker = ScalarPicker.singleton();

    @Test
    public void test_pickDottedIdentifier() {
        UnicodeProvider src = UnicodeProvider.wrap("a.b.c=");
        assertEquals("a.b.c", scalarPicker.pickDottedIdentifierOrThrow(src));
    }

    @Test
    public void test_pickString() {
        UnicodeProvider src = UnicodeProvider.wrap("hel\\lo world");
        assertEquals("hel\\lo", scalarPicker.pickString(src, ch -> ch != ' '));
        assertEquals(' ', src.next());
    }
}
