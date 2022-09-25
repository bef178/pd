package blackbox;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import pd.util.StringExtension;

public class Test_StringExtension {

    @Test
    public void test_split() {
        String s = "/";
        String[] a = StringExtension.split(s, '/');
        assertArrayEquals(new String[] { "", "" }, a);
    }

    @Test
    public void test_split_2() {
        String s = "a";
        String[] a = StringExtension.split(s, '/');
        assertArrayEquals(new String[] { "a" }, a);
    }
    
    @Test
    public void test_split_3() {
        String s = "a/b/c";
        String[] a = StringExtension.split(s, '/');
        assertArrayEquals(new String[] { "a", "b", "c" }, a);
    }
}
