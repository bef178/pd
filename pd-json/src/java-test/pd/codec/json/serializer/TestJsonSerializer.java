package pd.codec.json.serializer;

import org.junit.jupiter.api.Test;

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

public class TestJsonSerializer {

    JsonSerializer jsonSerializer = new JsonSerializer();

    @Test
    public void test_JsonCodec_convertToJson() {
        assertEquals(json5, jsonSerializer.serializeToJson(cat5));
    }

    @Test
    public void test_JsonCodec_serialize() {
        assertEquals(jsonText, jsonSerializer.serializeJson(json));
    }

    @Test
    public void test_JsonCodec_serialize2() {
        assertEquals(json2Text, jsonSerializer.serializeJson(json2));
    }

    @Test
    public void test_JsonCodec_serialize3_struct() {
        assertEquals(json3Text, jsonSerializer.serializeJson(json3));
    }

    @Test
    public void test_JsonCodec_serialize4_sequence() {
        assertEquals(json4Text, jsonSerializer.serializeJson(json4));
    }
}
