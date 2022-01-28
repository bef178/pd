package blackbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.json.JsonCodec.tokenFactory;

import org.junit.jupiter.api.Test;

import pd.json.IJsonArray;
import pd.json.IJsonString;
import pd.json.IJsonToken;
import pd.json.IJsonObject;
import pd.json.JsonCodec;

public class Test_Json {

    private static final String src1 = "\"9\n3d$fs冬你我他\""; // 冬: 0x2F81A, 194586
    private static final String src2 = "{\"Consume\":{\"Moneys\":{\"totalMoney\":\"0.01\"},\"record\":[{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:49:18.063+08:00\",\"GiveEgg\":\"0\",\"ID\":\"714833524\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:46:12.813+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1350320533\",\"OrderState\":\"3\",\"OrderStateName\":\"处理中\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"7\",\"PayWayID\":\"2\",\"PayWayName\":\"XXX邮政储蓄所\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"18\",\"CreateTime\":\"2008-09-05T08:40:55.703+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1413965649\",\"OrderState\":\"1\",\"OrderStateName\":\"成功\",\"OrderType\":\"4\",\"OrderTypeName\":\"同步交易订单\",\"PayID\":\"2\",\"PayWayID\":\"1\",\"PayWayName\":\"财付通在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"同步课程\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:39:29.127+08:00\",\"GiveEgg\":\"0\",\"ID\":\"83430389\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:38:33.28+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1206699325\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:31:54.453+08:00\",\"GiveEgg\":\"0\",\"ID\":\"795858378\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"}],\"results\":{\"totalRecords\":\"6\"}}}";

    @Test
    public void test_JsonTable_get() {
        IJsonObject token = JsonCodec.deserialize(src2, IJsonObject.class);
        String userId = token.get("Consume")
                .cast(IJsonObject.class).get("record")
                .cast(IJsonArray.class).get(5)
                .cast(IJsonObject.class).get("UserID")
                .cast(IJsonString.class).value();
        assertEquals("668288112", userId);
    }

    @Test
    public void test_JsonTokenFactory() {
        IJsonObject token = tokenFactory.newJsonObject()
                .put("ss", tokenFactory.newJsonString("hello"))
                .put("ii", tokenFactory.newJsonInt(77))
                .put("ff", tokenFactory.newJsonFloat(5.5))
                .put("ll", tokenFactory.newJsonArray()
                        .insert(tokenFactory.newJsonString("vava"))
                        .insert(tokenFactory.newJsonString("vbvb")))
                .put("nn", tokenFactory.newJsonNull())
                .put("bb", tokenFactory.newJsonBoolean(true));
        String serialized = JsonCodec.serialize(token);
        System.out.println(serialized);
        assertEquals("{\"ss\":\"hello\",\"ii\":77,\"ff\":5.5,\"ll\":[\"vava\",\"vbvb\"],\"nn\":null,\"bb\":true}", serialized);

        IJsonObject token2 = JsonCodec.deserialize(serialized, IJsonObject.class);
        String serialized2 = JsonCodec.serialize(token2);

        assertEquals(serialized, serialized2);
    }

    @Test
    public void test_JsonToken_serialize() {
        test_JsonToken_serialize(src1);
        test_JsonToken_serialize(src2);
    }

    private void test_JsonToken_serialize(String serialzied) {
        IJsonToken token2 = JsonCodec.deserialize(serialzied, IJsonToken.class);
        String serialized2 = JsonCodec.serialize(token2);
        assertEquals(serialzied, serialized2);

        IJsonToken token3 = JsonCodec.deserialize(serialized2, IJsonToken.class);
        String serialzied3 = JsonCodec.serialize(token3);
        assertEquals(serialzied, serialzied3);
    }
}
