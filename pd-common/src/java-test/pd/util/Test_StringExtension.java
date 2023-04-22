package pd.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

public class Test_StringExtension {

    @Test
    public void test_split() {
        test_split("/", '/', new String[] { "", "" });
        test_split("a", '/', new String[] { "a" });
        test_split("a/b/c", '/', new String[] { "a", "b", "c" });
    }

    private void test_split(String input, int inputSeparator, String[] expected) {
        String[] actual = StringExtension.split(input, inputSeparator);
        assertArrayEquals(expected, actual);
    }
}
