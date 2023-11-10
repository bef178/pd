package pd.codec.json.deserializer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import pd.codec.json.AirJson;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.JsonObject;
import pd.fun.Cat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.codec.json.BasicTest.cat5;
import static pd.codec.json.BasicTest.json;
import static pd.codec.json.BasicTest.json2;
import static pd.codec.json.BasicTest.json2Text;
import static pd.codec.json.BasicTest.json3;
import static pd.codec.json.BasicTest.json3Text;
import static pd.codec.json.BasicTest.json4;
import static pd.codec.json.BasicTest.json4Text;
import static pd.codec.json.BasicTest.json5;
import static pd.codec.json.BasicTest.jsonText;

public class TestDeserializer {

    AirJson airJson = new AirJson();

    @Test
    public void test_mapToObject() {
        assertEquals(cat5, airJson.mapToObject(json5, Cat.class));
    }

    @Test
    public void test_deserialize() {
        assertEquals(json, airJson.deserialize(jsonText));
    }

    @Test
    public void test_deserialize2() {
        assertEquals(json2, airJson.deserialize(json2Text));
    }

    @Test
    public void test_deserialize3_struct() {
        assertEquals(json3, airJson.deserialize(json3Text));
    }

    @Test
    public void test_deserialize4_sequence() {
        assertEquals(json4, airJson.deserialize(json4Text));
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
