package pd.codec.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.AbstractList;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import pd.fenc.ParsingException;

public class Test_JsonTypeConfig {

    @Test
    public void test_findTypeRef() {
        JsonTypeConfig config = new JsonTypeConfig();

        assertEquals(ArrayList.class, config.findTypeRef(List.class));
    }

    @Test
    public void test_findTypeRef_2() {
        JsonTypeConfig config = new JsonTypeConfig();
        config.registerTypeRef(AbstractList.class, ArrayList.class);
        config.registerTypeRef(List.class, AbstractList.class);

        assertEquals(ArrayList.class, config.findTypeRef(List.class));
        assertEquals(ArrayList.class, config.findTypeRef(AbstractList.class));
    }

    public void test_findPathRef() {
        JsonTypeConfig config = new JsonTypeConfig();
        config.registerPathRef("/[]", String.class);
        config.registerPathRef("/[2]", Map.Entry.class);

//        assertEquals(String.class, config.findPathRef("/[0]")); // TODO
        assertEquals(Map.Entry.class, config.findPathRef("/[2]"));
    }

    public void test_findTypeDescentRef() {
        JsonTypeConfig config = new JsonTypeConfig();
        config.registerTypeDescentRef(SimpleEntry.class, "/value", String.class);

        assertEquals(String.class, config.findTypeDescentRef(SimpleEntry.class, "/value"));
    }

    @Test
    public void test_registerTypeRef_fail() {
        JsonTypeConfig config = new JsonTypeConfig();

        ParsingException e = assertThrows(ParsingException.class, () -> {
            config.registerTypeRef(List.class, List.class);
        });
        assertEquals("E: cannot map one type to itself", e.getMessage());
    }

    @Test
    public void test_registerTypeRef_fail_2() {
        JsonTypeConfig config = new JsonTypeConfig();

        ParsingException e = assertThrows(ParsingException.class, () -> {
            config.registerTypeRef(List.class, Map.class);
        });
        assertEquals("E: should map one type to its derived type", e.getMessage());
    }
}
