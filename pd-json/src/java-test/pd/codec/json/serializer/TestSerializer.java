package pd.codec.json.serializer;

import org.junit.jupiter.api.Test;
import pd.codec.json.AirJson;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.codec.json.datatype.TestJson.cat5;
import static pd.codec.json.datatype.TestJson.json;
import static pd.codec.json.datatype.TestJson.json2;
import static pd.codec.json.datatype.TestJson.json2Text;
import static pd.codec.json.datatype.TestJson.json3;
import static pd.codec.json.datatype.TestJson.json3Text;
import static pd.codec.json.datatype.TestJson.json4;
import static pd.codec.json.datatype.TestJson.json4Text;
import static pd.codec.json.datatype.TestJson.json5;
import static pd.codec.json.datatype.TestJson.jsonText;

public class TestSerializer {

    AirJson airJson = new AirJson();

    @Test
    public void test_mapToJson() {
        assertEquals(json5, airJson.mapToJson(cat5));
    }

    @Test
    public void test_serialize() {
        assertEquals(jsonText, airJson.serialize(json));
    }

    @Test
    public void test_serialize2() {
        assertEquals(json2Text, airJson.serialize(json2));
    }

    @Test
    public void test_serialize3_struct() {
        assertEquals(json3Text, airJson.serialize(json3));
    }

    @Test
    public void test_serialize4_sequence() {
        assertEquals(json4Text, airJson.serialize(json4));
    }
}
