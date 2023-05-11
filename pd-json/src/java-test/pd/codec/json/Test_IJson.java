package pd.codec.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static pd.codec.json.Test_JsonCodec.json2;

import org.junit.jupiter.api.Test;
import pd.codec.json.datafactory.IJsonFactory;
import pd.codec.json.datatype.IJson;

public class Test_IJson {

    private static final IJsonFactory f = IJsonFactory.getFactory();

    @Test
    public void test_IJson_equals_boolean() {
        IJson json = f.createJsonBoolean(true);
        IJson json2 = f.createJsonBoolean(true);
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void test_IJson_equals_float64() {
        IJson json = f.createJsonNumber(1000099.0);
        IJson json2 = f.createJsonNumber(1000099.0D);
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void test_IJson_equals_int64() {
        IJson json = f.createJsonNumber(1000099);
        IJson json2 = f.createJsonNumber(1000099L);
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void test_IJson_equals_null() {
        IJson json = f.getJsonNull();
        IJson json2 = f.getJsonNull();
        assertSame(json, json2);
    }

    @Test
    public void test_IJson_equals_sequence() {
        IJson json = f.createJsonArray()
                .append(f.createJsonString("a"))
                .append(f.createJsonNumber(2));
        IJson json2 = f.createJsonArray()
                .append(f.createJsonString("a"))
                .append(f.createJsonNumber(2));
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void test_IJson_equals_string() {
        IJson json = f.createJsonString("yes");
        IJson json2 = f.createJsonString("yes");
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void test_IJson_equals_struct() {
        IJson json = f.createJsonObject()
                .set("a", f.createJsonString("1"))
                .set("b", f.createJsonNumber(2));
        IJson json2 = f.createJsonObject()
                .set("a", f.createJsonString("1"))
                .set("b", f.createJsonNumber(2));
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
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
