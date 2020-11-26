package blackbox;

import pd.net.uri.Path;

public class Test_Path {

    public static void main(String[] args) {
        test_getBasename();
        test_getParent();

        System.out.println("all done");
    }

    private static void test_getBasename() {
        String[] inputs = new String[] {
                "abc", "abc/def", "abc///", "abc//.", "abc//.///", "////", "",
        };
        String[] expecteds = new String[] {
                "abc", "def", "abc", ".", ".", "/", "",
        };

        for (int i = 0; i < inputs.length; i++) {
            assert Path.getBasename(inputs[i]).equals(expecteds[i]) : "E: check getBasename: " + inputs[i];
        }
    }

    private static void test_getParent() {
        String[] inputs = new String[] {
                "abc", "abc//", "abc/../def", "/abc", "/", "abc//.///", "",
        };
        String[] expecteds = new String[] {
                ".", ".", "abc/..", "/", "/", "abc", ".",
        };

        for (int i = 0; i < inputs.length; i++) {
            assert Path.getParent(inputs[i]).equals(expecteds[i]) : "E: check getParent: " + inputs[i];
        }
    }
}
