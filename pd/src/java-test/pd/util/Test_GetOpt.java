package pd.util;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test_GetOpt {

    @Test
    public void testShortOptString() {
        Assertions.assertTrue("-A".matches(GetOpt.singleAlphanumericOptRegex));
        Assertions.assertTrue("-a:".matches(GetOpt.singleAlphanumericOptRegex));
        Assertions.assertTrue("-0".matches(GetOpt.singleAlphanumericOptRegex));

        GetOpt getOpt = new GetOpt().opt("-A");
        Assertions.assertTrue(getOpt.options.containsKey("-A"));
        Assertions.assertFalse(getOpt.options.get("-A"));

        getOpt = new GetOpt().opt("-a:");
        Assertions.assertTrue(getOpt.options.containsKey("-a"));
        Assertions.assertTrue(getOpt.options.get("-a"));
    }

    @Test
    public void testLongOptString() {
        Assertions.assertTrue(GetOpt.longOptRegexPattern.matcher("--00").matches());

        GetOpt getOpt = new GetOpt().opt("--a-long");
        Assertions.assertTrue(getOpt.options.containsKey("--a-long"));
        Assertions.assertFalse(getOpt.options.get("--a-long"));

        getOpt = new GetOpt().opt("--b_long");
        Assertions.assertTrue(getOpt.options.containsKey("--b_long"));
        Assertions.assertFalse(getOpt.options.get("--b_long"));

        Assertions.assertThrows(IllegalArgumentException.class, () -> new GetOpt().opt("--c__long"));

        getOpt = new GetOpt().opt("--optionExpectingArgument:");
        Assertions.assertTrue(getOpt.options.containsKey("--optionExpectingArgument"));
        Assertions.assertTrue(getOpt.options.get("--optionExpectingArgument"));
    }

    @Test
    public void testGetOpt() {
        GetOpt getOpt = new GetOpt().opt("-u:,-D:,--user:,-h");
        List<Map.Entry<String, String>> parsedOptions = getOpt.parse(new String[] {
                "-u", "user", "-uUser", "-Duser=User", "-h", "path", "-p"
        });

        Map.Entry<String, String> entry = parsedOptions.get(0);
        Assertions.assertEquals("-u", entry.getKey());
        Assertions.assertEquals("user", entry.getValue());

        entry = parsedOptions.get(1);
        Assertions.assertEquals("-u", entry.getKey());
        Assertions.assertEquals("User", entry.getValue());

        entry = parsedOptions.get(2);
        Assertions.assertEquals("-D", entry.getKey());
        Assertions.assertEquals("user=User", entry.getValue());

        entry = parsedOptions.get(3);
        Assertions.assertEquals("-h", entry.getKey());
        Assertions.assertNull(entry.getValue());

        entry = parsedOptions.get(4);
        Assertions.assertEquals("!opt", entry.getKey());
        Assertions.assertEquals("path", entry.getValue());

        entry = parsedOptions.get(5);
        Assertions.assertEquals("!opt", entry.getKey());
        Assertions.assertEquals("-p", entry.getValue());
    }
}
