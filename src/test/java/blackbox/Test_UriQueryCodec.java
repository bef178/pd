package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import pd.codec.UriQueryCodec;

public class Test_UriQueryCodec {

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
    public void test_parse() {
        assertEquals(queryList, UriQueryCodec.parse(queryString));
    }

    @Test
    public void test_toQueryString() {
        assertEquals(queryStringNormalized, UriQueryCodec.toQueryString(queryList));
    }
}
