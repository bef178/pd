package pd.codec.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pd.entity.Cat;

public class Test_JsonCodec {

    private static final JsonCodec jsonCodec = JsonCodec.singleton();

    private static final IJsonFactory f = JsonCodec.f;

    private static final IJson json;
    private static final String jsonText;

    static final IJson json2;
    static final String json2Text;

    private static final IJson json3;
    private static final String json3Text;

    private static final IJson json4;
    private static final String json4Text;

    private static final IJson json5;
    private static final Cat cat5;

    static {
        json = f.createJsonString().set("9\n3d$fs冬你我他");
        jsonText = "\"9\\n3d$fs冬你我他\""; // 冬: 0x2F81A, 194586

        json2 = f.createJsonObject()
                .set("consume", f.createJsonObject()
                        .set("amount", f.createJsonObject()
                                .set("totalMoney", f.createJsonString("0.01")))
                        .set("records", f.createJsonArray()
                                .append(f.createJsonObject()
                                        .set("AppID", f.createJsonNumber(11))
                                        .set("CreateTime", f.createJsonString("2008-09-05T08:49:18.063+08:00"))
                                        .set("GiveEgg", f.createJsonString("0"))
                                        .set("ID", f.createJsonString("714833524"))
                                        .set("OrderState", f.createJsonString("2"))
                                        .set("OrderStateName", f.createJsonString("失败"))
                                        .set("OrderType", f.createJsonString("3"))
                                        .set("OrderTypeName", f.createJsonString("实物交易订单"))
                                        .set("PayID", f.createJsonString("3"))
                                        .set("PayWayID", f.createJsonString("1"))
                                        .set("PayWayName", f.createJsonString("快钱在线支付"))
                                        .set("TotalMoney", f.createJsonString("0.01"))
                                        .set("TotalNumber", f.createJsonString("0"))
                                        .set("TradeSource", f.createJsonString("商城购物"))
                                        .set("UserID", f.createJsonString("668288112")))
                                .append(f.createJsonObject()
                                        .set("AppID", f.createJsonNumber(18))
                                        .set("CreateTime", f.createJsonString("2008-09-05T08:40:55.703+08:00"))
                                        .set("GiveEgg", f.createJsonString("0"))
                                        .set("ID", f.createJsonString("1413965649"))
                                        .set("OrderState", f.createJsonString("1"))
                                        .set("OrderStateName", f.createJsonString("成功"))
                                        .set("OrderType", f.createJsonString("4"))
                                        .set("OrderTypeName", f.createJsonString("同步交易订单"))
                                        .set("PayID", f.createJsonString("2"))
                                        .set("PayWayID", f.createJsonString("1"))
                                        .set("PayWayName", f.createJsonString("财付通在线支付"))
                                        .set("TotalMoney", f.createJsonString("0.01"))
                                        .set("TotalNumber", f.createJsonString("0"))
                                        .set("TradeSource", f.createJsonString("同步课程"))
                                        .set("UserID", f.createJsonString("668288112"))))
                        .set("result", f.createJsonObject()
                                .set("totalRecords", f.createJsonString("2"))));
        json2Text = "{\"consume\":{\"amount\":{\"totalMoney\":\"0.01\"},\"records\":[{\"AppID\":11,\"CreateTime\":\"2008-09-05T08:49:18.063+08:00\",\"GiveEgg\":\"0\",\"ID\":\"714833524\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":18,\"CreateTime\":\"2008-09-05T08:40:55.703+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1413965649\",\"OrderState\":\"1\",\"OrderStateName\":\"成功\",\"OrderType\":\"4\",\"OrderTypeName\":\"同步交易订单\",\"PayID\":\"2\",\"PayWayID\":\"1\",\"PayWayName\":\"财付通在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"同步课程\",\"UserID\":\"668288112\"}],\"result\":{\"totalRecords\":\"2\"}}}";

        json3 = f.createJsonObject()
                .set("a", f.createJsonNumber(1))
                .set("b", f.createJsonNumber(2));
        json3Text = "{\"a\":1,\"b\":2}";

        json4 = f.createJsonArray()
                .append(f.createJsonString("a"))
                .append(f.createJsonString("b"));
        json4Text = "[\"a\",\"b\"]";

        json5 = f.createJsonObject()
                .set("Phylum", f.createJsonString("Chordata"))
                .set("Class", f.createJsonString("Mammalia"))
                .set("Order", f.createJsonString("Carnivora"))
                .set("Family", f.createJsonString("Felidae"))
                .set("Genus", f.createJsonString("Felis"))
                .set("Species", f.createJsonString("Felis catus"))
                .set("name", f.createJsonString("Mimi"));
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
    public void test_JsonCodec_convertToJava() {
        assertEquals(cat5, jsonCodec.convertToJava(json5, Cat.class));
    }

    @Test
    public void test_JsonCodec_convertToJson() {
        assertEquals(json5, jsonCodec.convertToJson(cat5));
    }

    @Test
    public void test_JsonCodec_deserialize() {
        assertEquals(json, jsonCodec.deserialize(jsonText));
    }

    @Test
    public void test_JsonCodec_deserialize2() {
        assertEquals(json2, jsonCodec.deserialize(json2Text));
    }

    @Test
    public void test_JsonCodec_deserialize3_struct() {
        assertEquals(json3, jsonCodec.deserialize(json3Text));
    }

    @Test
    public void test_JsonCodec_deserialize4_sequence() {
        assertEquals(json4, jsonCodec.deserialize(json4Text));
    }

    @Test
    public void test_JsonCodec_serialize() {
        assertEquals(jsonText, jsonCodec.serialize(json));
    }

    @Test
    public void test_JsonCodec_serialize2() {
        assertEquals(json2Text, jsonCodec.serialize(json2));
    }

    @Test
    public void test_JsonCodec_serialize3_struct() {
        assertEquals(json3Text, jsonCodec.serialize(json3));
    }

    @Test
    public void test_JsonCodec_serialize4_sequence() {
        assertEquals(json4Text, jsonCodec.serialize(json4));
    }
}
