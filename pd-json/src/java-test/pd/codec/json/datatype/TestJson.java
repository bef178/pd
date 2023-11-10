package pd.codec.json.datatype;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pd.codec.json.AirJson;
import pd.codec.json.datafactory.JsonFactory;
import pd.codec.json.datatype.Json;
import pd.codec.json.datatype.JsonObject;
import pd.fun.Cat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestJson {

    private static final JsonFactory jsonFactory = JsonFactory.getFactory();

    public static final Json json;
    public static final String jsonText;

    public static final Json json2;
    public static final String json2Text;

    public static final Json json3;
    public static final String json3Text;

    public static final Json json4;
    public static final String json4Text;

    public static final Cat cat5;
    public static final Json json5;

    static {
        json = jsonFactory.createJsonString().set("9\n3d$fs冬你我他");
        jsonText = "\"9\\n3d$fs冬你我他\""; // 冬: 0x2F81A, 194586

        json2 = jsonFactory.createJsonObject()
                .set("consume", jsonFactory.createJsonObject()
                        .set("amount", jsonFactory.createJsonObject()
                                .set("totalMoney", jsonFactory.createJsonString("0.01")))
                        .set("records", jsonFactory.createJsonArray()
                                .append(jsonFactory.createJsonObject()
                                        .set("AppID", jsonFactory.createJsonNumber(11))
                                        .set("CreateTime", jsonFactory.createJsonString("2008-09-05T08:49:18.063+08:00"))
                                        .set("GiveEgg", jsonFactory.createJsonString("0"))
                                        .set("ID", jsonFactory.createJsonString("714833524"))
                                        .set("OrderState", jsonFactory.createJsonString("2"))
                                        .set("OrderStateName", jsonFactory.createJsonString("失败"))
                                        .set("OrderType", jsonFactory.createJsonString("3"))
                                        .set("OrderTypeName", jsonFactory.createJsonString("实物交易订单"))
                                        .set("PayID", jsonFactory.createJsonString("3"))
                                        .set("PayWayID", jsonFactory.createJsonString("1"))
                                        .set("PayWayName", jsonFactory.createJsonString("快钱在线支付"))
                                        .set("TotalMoney", jsonFactory.createJsonString("0.01"))
                                        .set("TotalNumber", jsonFactory.createJsonString("0"))
                                        .set("TradeSource", jsonFactory.createJsonString("商城购物"))
                                        .set("UserID", jsonFactory.createJsonString("668288112")))
                                .append(jsonFactory.createJsonObject()
                                        .set("AppID", jsonFactory.createJsonNumber(18))
                                        .set("CreateTime", jsonFactory.createJsonString("2008-09-05T08:40:55.703+08:00"))
                                        .set("GiveEgg", jsonFactory.createJsonString("0"))
                                        .set("ID", jsonFactory.createJsonString("1413965649"))
                                        .set("OrderState", jsonFactory.createJsonString("1"))
                                        .set("OrderStateName", jsonFactory.createJsonString("成功"))
                                        .set("OrderType", jsonFactory.createJsonString("4"))
                                        .set("OrderTypeName", jsonFactory.createJsonString("同步交易订单"))
                                        .set("PayID", jsonFactory.createJsonString("2"))
                                        .set("PayWayID", jsonFactory.createJsonString("1"))
                                        .set("PayWayName", jsonFactory.createJsonString("财付通在线支付"))
                                        .set("TotalMoney", jsonFactory.createJsonString("0.01"))
                                        .set("TotalNumber", jsonFactory.createJsonString("0"))
                                        .set("TradeSource", jsonFactory.createJsonString("同步课程"))
                                        .set("UserID", jsonFactory.createJsonString("668288112"))))
                        .set("result", jsonFactory.createJsonObject()
                                .set("totalRecords", jsonFactory.createJsonString("2"))));
        json2Text = "{\"consume\":{\"amount\":{\"totalMoney\":\"0.01\"},\"records\":[{\"AppID\":11,\"CreateTime\":\"2008-09-05T08:49:18.063+08:00\",\"GiveEgg\":\"0\",\"ID\":\"714833524\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":18,\"CreateTime\":\"2008-09-05T08:40:55.703+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1413965649\",\"OrderState\":\"1\",\"OrderStateName\":\"成功\",\"OrderType\":\"4\",\"OrderTypeName\":\"同步交易订单\",\"PayID\":\"2\",\"PayWayID\":\"1\",\"PayWayName\":\"财付通在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"同步课程\",\"UserID\":\"668288112\"}],\"result\":{\"totalRecords\":\"2\"}}}";

        json3 = jsonFactory.createJsonObject()
                .set("a", jsonFactory.createJsonNumber(1))
                .set("b", jsonFactory.createJsonNumber(2));
        json3Text = "{\"a\":1,\"b\":2}";

        json4 = jsonFactory.createJsonArray()
                .append(jsonFactory.createJsonString("a"))
                .append(jsonFactory.createJsonString("b"));
        json4Text = "[\"a\",\"b\"]";

        json5 = jsonFactory.createJsonObject()
                .set("Phylum", jsonFactory.createJsonString("Chordata"))
                .set("Class", jsonFactory.createJsonString("Mammalia"))
                .set("Order", jsonFactory.createJsonString("Carnivora"))
                .set("Family", jsonFactory.createJsonString("Felidae"))
                .set("Genus", jsonFactory.createJsonString("Felis"))
                .set("Species", jsonFactory.createJsonString("Felis catus"))
                .set("name", jsonFactory.createJsonString("Mimi"));
        cat5 = new Cat();
        cat5.Phylum = "Chordata";
        cat5.Class = "Mammalia";
        cat5.Order = "Carnivora";
        cat5.Family = "Felidae";
        cat5.Genus = "Felis";
        cat5.Species = "Felis catus";
        cat5.name = "Mimi";
    }

    @Test
    public void test_JsonFactory() {
        JsonObject json = jsonFactory.createJsonObject()
                .set("ss", jsonFactory.createJsonString("ss"))
                .set("ii", jsonFactory.createJsonNumber(77))
                .set("ff", jsonFactory.createJsonNumber(5.5))
                .set("aa", jsonFactory.createJsonArray()
                        .append(jsonFactory.createJsonString("xx"))
                        .append(jsonFactory.createJsonString("yy")))
                .set("nn", jsonFactory.getJsonNull())
                .set("bb", jsonFactory.createJsonBoolean(true))
                .set("oo", jsonFactory.createJsonObject());
        String jsonText = "{\"ss\":\"ss\",\"ii\":77,\"ff\":5.5,\"aa\":[\"xx\",\"yy\"],\"bb\":true,\"oo\":{}}";

        Assertions.assertEquals(jsonText, new AirJson().serialize(json));

        JsonObject echo = new AirJson().deserialize(jsonText).asJsonObject();
        assertEquals(jsonText, new AirJson().serialize(echo));

        assertEquals(json, echo);
    }

    @Test
    public void testJsonBooleanEquals() {
        Json json = jsonFactory.createJsonBoolean(true);
        Json json2 = jsonFactory.createJsonBoolean(true);
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void testJsonFloat64Equals() {
        Json json = jsonFactory.createJsonNumber(1000099.0);
        Json json2 = jsonFactory.createJsonNumber(1000099.0D);
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void testJsonInt64Equals() {
        Json json = jsonFactory.createJsonNumber(1000099);
        Json json2 = jsonFactory.createJsonNumber(1000099L);
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void testJsonNullEquals() {
        Json json = jsonFactory.getJsonNull();
        Json json2 = jsonFactory.getJsonNull();
        assertSame(json, json2);
    }

    @Test
    public void testJsonStringEquals() {
        Json json = jsonFactory.createJsonString("yes");
        Json json2 = jsonFactory.createJsonString("yes");
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void testJsonArrayEquals() {
        Json json = jsonFactory.createJsonArray()
                .append(jsonFactory.createJsonString("a"))
                .append(jsonFactory.createJsonNumber(2));
        Json json2 = jsonFactory.createJsonArray()
                .append(jsonFactory.createJsonString("a"))
                .append(jsonFactory.createJsonNumber(2));
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void testJsonObjectEquals() {
        Json json = jsonFactory.createJsonObject()
                .set("a", jsonFactory.createJsonString("1"))
                .set("b", jsonFactory.createJsonNumber(2));
        Json json2 = jsonFactory.createJsonObject()
                .set("a", jsonFactory.createJsonString("1"))
                .set("b", jsonFactory.createJsonNumber(2));
        assertNotSame(json, json2);
        assertEquals(json, json2);
        assertEquals(json.hashCode(), json2.hashCode());
    }

    @Test
    public void test_JsonNumber_isRoundNumber() {
        assertTrue(jsonFactory.createJsonNumber(4294967296L).isRoundNumber());
        assertFalse(jsonFactory.createJsonNumber(5.000001).isRoundNumber());
    }

    @Test
    public void testJsonGet() {
        assertEquals("1413965649", json2
                .asJsonObject().get("consume")
                .asJsonObject().get("records")
                .asJsonArray().get(1)
                .asJsonObject().get("ID")
                .asJsonString().getString());
    }
}
