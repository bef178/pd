package pd.codec.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_JsonCodec {

    private static final JsonCodec jsonCodec = new JsonCodec();

    private static final String jsonText1 = "\"9\\n3d$fs冬你我他\""; // 冬: 0x2F81A, 194586
    private static final String jsonText2 = "{\"Consume\":{\"Moneys\":{\"totalMoney\":\"0.01\"},\"record\":[{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:49:18.063+08:00\",\"GiveEgg\":\"0\",\"ID\":\"714833524\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:46:12.813+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1350320533\",\"OrderState\":\"3\",\"OrderStateName\":\"处理中\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"7\",\"PayWayID\":\"2\",\"PayWayName\":\"XXX邮政储蓄所\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"18\",\"CreateTime\":\"2008-09-05T08:40:55.703+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1413965649\",\"OrderState\":\"1\",\"OrderStateName\":\"成功\",\"OrderType\":\"4\",\"OrderTypeName\":\"同步交易订单\",\"PayID\":\"2\",\"PayWayID\":\"1\",\"PayWayName\":\"财付通在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"同步课程\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:39:29.127+08:00\",\"GiveEgg\":\"0\",\"ID\":\"83430389\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:38:33.28+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1206699325\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:31:54.453+08:00\",\"GiveEgg\":\"0\",\"ID\":\"795858378\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"}],\"results\":{\"totalRecords\":\"6\"}}}";

    @Test
    public void test_Json_get() {
        String userId = jsonCodec.deserialize(jsonText2).asJsonObject().get("Consume")
                .asJsonObject().get("record")
                .asJsonArray().get(5)
                .asJsonObject().get("UserID")
                .asJsonString().getString();
        assertEquals("668288112", userId);
    }

    @Test
    public void test_serialize_deserialize() {
        test_serialize_deserialize(jsonText1);
        test_serialize_deserialize(jsonText2);
    }

    private void test_serialize_deserialize(String jsonText) {
        IJson json2 = jsonCodec.deserialize(jsonText);
        String jsonText2 = jsonCodec.serialize(json2);
        assertEquals(jsonText, jsonText2);

        IJson json3 = jsonCodec.deserialize(jsonText2);
        String jsonText3 = jsonCodec.serialize(json3);
        assertEquals(jsonText2, jsonText3);
    }

    @Test
    public void test_JsonFactory() {
        IJsonFactory factory = new SimpleJsonFactory();
        IJsonObject jsonObject = factory.getJsonObject()
                .set("ss", factory.getJsonString().set("hello"))
                .set("ii", factory.getJsonNumber().set(77))
                .set("ff", factory.getJsonNumber().set(5.5))
                .set("ll", factory.getJsonArray()
                        .append(factory.getJsonString().set("vava"))
                        .append(factory.getJsonString().set("vbvb")))
                .set("nn", factory.getJsonNull())
                .set("bb", factory.getJsonBoolean(true));
        String jsonText = jsonCodec.serialize(jsonObject);
        assertEquals("{\"ss\":\"hello\",\"ii\":77,\"ff\":5.5,\"ll\":[\"vava\",\"vbvb\"],\"nn\":null,\"bb\":true}",
                jsonText);

        IJsonObject json2 = jsonCodec.deserialize(jsonText).asJsonObject();
        String jsonText2 = jsonCodec.serialize(json2);

        assertEquals(jsonText, jsonText2);
    }
}
