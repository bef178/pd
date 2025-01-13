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

    @Test
    public void test_split_n() {
        assertArrayEquals(new String[] { "ababa" }, StringExtension.split("ababa", 'b', 1));

        assertArrayEquals(new String[] { "a", "aba" }, StringExtension.split("ababa", 'b', 2));

        assertArrayEquals(new String[] { "a", "a", "a" }, StringExtension.split("ababa", 'b', 3));

        assertArrayEquals(new String[] { "a", "a", "a" }, StringExtension.split("ababa", 'b', 9));
    }
}
