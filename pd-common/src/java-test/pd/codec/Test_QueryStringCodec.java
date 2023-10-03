package pd.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class Test_QueryStringCodec {

    private static final List<SimpleImmutableEntry<String, String>> queryList;
    private static final String queryString;
    private static final String queryStringNormalized;

    static {
        queryList = new ArrayList<>();
        queryList.add(new SimpleImmutableEntry<>("a", ""));
        queryList.add(new SimpleImmutableEntry<>("b", "1"));
        queryString = "a&b=1";
        queryStringNormalized = "a=&b=1";
    }

    @Test
    public void test_serialize() {
        assertEquals(queryStringNormalized, QueryStringCodec.serialize(queryList));
    }

    @Test
    public void test_deserialize() {
        assertEquals(queryList, QueryStringCodec.deserialize(queryString));
    }
}
