package pd.codec.json.mapper.json2javaobject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import pd.codec.json.datafactory.IJsonFactory;
import pd.codec.json.datatype.IJsonObject;
import pd.codec.json.JsonCodec;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_JsonToJavaObjectConverter {

    private static final IJsonFactory f = JsonCodec.f;

    @Test
    public void test_convertToDefaultObject() {
        IJsonObject jsonObject = f.createJsonObject()
                .set("a", f.createJsonArray()
                        .append(f.createJsonString("ae1")))
                .set("b", f.createJsonBoolean(true))
                .set("f", f.createJsonNumber(0.1))
                .set("i", f.createJsonNumber(42))
                .set("m", f.createJsonObject()
                        .set("mk1", f.createJsonString("mv1")))
                .set("s", f.createJsonString("s1"));
        Object o = JsonCodec.singleton().convertToJavaObject(jsonObject, Object.class);

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

        assertEquals(LinkedHashMap.class, map.get("m").getClass());
        @SuppressWarnings("unchecked")
        Map<String, Object> m = (Map<String, Object>) map.get("m");
        assertEquals(1, m.size());
        assertEquals(String.class, m.get("mk1").getClass());
        assertEquals("mv1", m.get("mk1"));

        assertEquals(String.class, map.get("s").getClass());
        String s = (String) map.get("s");
        assertEquals("s1", s);
    }
}
