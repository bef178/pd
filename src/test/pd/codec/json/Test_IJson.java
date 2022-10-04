package pd.codec.json;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.codec.json.Test_JsonSerializer.json2;

import org.junit.jupiter.api.Test;

public class Test_IJson {

    private static final IJsonFactory f = new SimpleJsonFactory();

    @Test
    public void test_IJson_equals_boolean() {
        IJson json = f.createJsonBoolean(true);
        IJson json2 = f.createJsonBoolean(true);
        assertTrue(json != json2);
        assertTrue(json.equals(json2));
        assertTrue(json.hashCode() == json2.hashCode());
    }

    @Test
    public void test_IJson_equals_float64() {
        IJson json = f.createJsonNumber(1000099.0);
        IJson json2 = f.createJsonNumber(1000099.0D);
        assertTrue(json != json2);
        assertTrue(json.equals(json2));
        assertTrue(json.hashCode() == json2.hashCode());
    }

    @Test
    public void test_IJson_equals_int64() {
        IJson json = f.createJsonNumber(1000099);
        IJson json2 = f.createJsonNumber(1000099L);
        assertTrue(json != json2);
        assertTrue(json.equals(json2));
        assertTrue(json.hashCode() == json2.hashCode());
    }

    @Test
    public void test_IJson_equals_null() {
        IJson json = f.getJsonNull();
        IJson json2 = f.getJsonNull();
        assertTrue(json == json2);
    }

    @Test
    public void test_IJson_equals_sequence() {
        IJson json = f.createJsonArray()
                .append(f.createJsonString("a"))
                .append(f.createJsonNumber(2));
        IJson json2 = f.createJsonArray()
                .append(f.createJsonString("a"))
                .append(f.createJsonNumber(2));
        assertTrue(json != json2);
        assertTrue(json.equals(json2));
        assertTrue(json.hashCode() == json2.hashCode());
    }

    @Test
    public void test_IJson_equals_string() {
        IJson json = f.createJsonString("yes");
        IJson json2 = f.createJsonString("yes");
        assertTrue(json != json2);
        assertTrue(json.equals(json2));
        assertTrue(json.hashCode() == json2.hashCode());
    }

    @Test
    public void test_IJson_equals_struct() {
        IJson json = f.createJsonObject()
                .set("a", f.createJsonString("1"))
                .set("b", f.createJsonNumber(2));
        IJson json2 = f.createJsonObject()
                .set("a", f.createJsonString("1"))
                .set("b", f.createJsonNumber(2));
        assertTrue(json != json2);
        assertTrue(json.equals(json2));
        assertTrue(json.hashCode() == json2.hashCode());
    }

    @Test
    public void test_IJson_get() {
        assertEquals("1413965649", json2
                .asJsonObject().get("consume")
                .asJsonObject().get("records")
                .asJsonArray().get(1)
                .asJsonObject().get("ID")
                .asJsonString().getString());
    }
}
