package pd.uri;

import java.util.Collections;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestQueryObject {

    @Test
    public void testParse() {
        QueryObject queryObject = QueryObject.parse("a&b=1");
        assertEquals("a=&b=1", queryObject.toString());

        assertEquals(2, queryObject.params.size());
        assertEquals(Collections.singletonList(""), queryObject.get("a"));
        assertEquals(Collections.singletonList("1"), queryObject.get("b"));
    }
}
