package pd.codec.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_JsonFactory {

    @Test
    public void test_JsonFactory() {
        IJsonFactory factory = new SimpleJsonFactory();

        IJsonObject json = factory.createJsonObject()
                .set("ss", factory.createJsonString("hello"))
                .set("ii", factory.createJsonNumber(77))
                .set("ff", factory.createJsonNumber(5.5))
                .set("ll", factory.createJsonArray()
                        .append(factory.createJsonString("vava"))
                        .append(factory.createJsonString("vbvb")))
                .set("nn", factory.getJsonNull())
                .set("bb", factory.createJsonBoolean(true));
        String jsonText = "{\"ss\":\"hello\",\"ii\":77,\"ff\":5.5,\"ll\":[\"vava\",\"vbvb\"],\"nn\":null,\"bb\":true}";
        assertEquals(jsonText, JsonCodec.serialize(json, null));

        IJsonObject json2 = JsonCodec.deserialize(jsonText).asJsonObject();
        assertEquals(jsonText, JsonCodec.serialize(json2, null));
    }
}
