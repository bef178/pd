package pd.util;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_QueryObject {

    @Test
    public void testParse() {
        QueryObject queryObject = QueryObject.parse("a&b=1");
        assertEquals("a=&b=1", queryObject.toString());

        assertEquals(2, queryObject.params.size());
        assertEquals(Collections.singletonList(""), queryObject.get("a"));
        assertEquals(Collections.singletonList("1"), queryObject.get("b"));
    }

    @Test
    public void testParseEmpty() {
        QueryObject queryObject = QueryObject.parse("");
        assertEquals("", queryObject.toString());
    }
}
