package pd.json.deserializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pd.fun.Cat;
import pd.json.AirJson;
import pd.json.datafactory.JsonFactory;
import pd.json.datatype.JsonObject;
import pd.json.datatype.TestJson;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDeserializer {

    AirJson airJson = new AirJson();

    @Test
    public void test_mapToObject() {
        assertEquals(TestJson.cat5, airJson.mapToObject(TestJson.json5, Cat.class));
    }

    @Test
    public void test_deserialize() {
        Assertions.assertEquals(TestJson.json, airJson.deserialize(TestJson.jsonText));
    }

    @Test
    public void test_deserialize2() {
        Assertions.assertEquals(TestJson.json2, airJson.deserialize(TestJson.json2Text));
    }

    @Test
    public void test_deserialize3_struct() {
        Assertions.assertEquals(TestJson.json3, airJson.deserialize(TestJson.json3Text));
    }

    @Test
    public void test_deserialize4_sequence() {
        Assertions.assertEquals(TestJson.json4, airJson.deserialize(TestJson.json4Text));
    }

    @Test
    public void testDeserializeToDefaultObject() {
        JsonFactory jsonFactory = airJson.getJsonFactory();
        JsonObject jsonObject = jsonFactory.createJsonObject()
                .set("a", jsonFactory.createJsonArray()
                        .append(jsonFactory.createJsonString("ae1")))
                .set("b", jsonFactory.createJsonBoolean(true))
                .set("f", jsonFactory.createJsonNumber(0.1))
                .set("i", jsonFactory.createJsonNumber(42))
                .set("o", jsonFactory.createJsonObject()
                        .set("ok1", jsonFactory.createJsonString("op1")))
                .set("s", jsonFactory.createJsonString("s1"));

        Object o = airJson.mapToObject(jsonObject, Object.class);

        assertEquals(LinkedHashMap.class, o.getClass());

        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) o;
        assertEquals(ArrayList.class, map.get("a").getClass());

        @SuppressWarnings("unchecked")
        ArrayList<Object> a = (ArrayList<Object>) map.get("a");
        assertEquals(1, a.size());

        assertEquals(String.class, a.get(0).getClass());
        assertEquals("ae1", a.get(0));

        assertEquals(Boolean.class, map.get("b").getClass());
        Boolean b = (Boolean) map.get("b");
        assertEquals(true, b);

        assertEquals(Double.class, map.get("f").getClass());
        Double f = (Double) map.get("f");
        assertEquals(0.1, f);

        assertEquals(Long.class, map.get("i").getClass());
        Long i = (Long) map.get("i");
        assertEquals(42, i);

        assertEquals(LinkedHashMap.class, map.get("o").getClass());

        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) map.get("o");
        assertEquals(1, m.size());

        assertEquals(String.class, m.get("ok1").getClass());
        assertEquals("op1", m.get("ok1"));

        assertEquals(String.class, map.get("s").getClass());
        String s = (String) map.get("s");
        assertEquals("s1", s);
    }
}
