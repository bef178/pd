package junit;

import java.io.IOException;

import libcliff.json.Factory;
import libcliff.json.Json;
import libcliff.json.JsonDict;
import libcliff.json.JsonList;
import libcliff.json.JsonScalar;
import libcliff.json.Producer;
import libcliff.json.mono.MonoJsonProducer;
import org.junit.Assert;
import org.junit.Test;

public class TestJson {

    static String src1 = "{\"Consume\":{\"Moneys\":{\"totalMoney\":\"0.01\"},\"record\":[{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:49:18.063+08:00\",\"GiveEgg\":\"0\",\"ID\":\"714833524\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:46:12.813+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1350320533\",\"OrderState\":\"3\",\"OrderStateName\":\"处理中\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"7\",\"PayWayID\":\"2\",\"PayWayName\":\"XXX邮政储蓄所\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"18\",\"CreateTime\":\"2008-09-05T08:40:55.703+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1413965649\",\"OrderState\":\"1\",\"OrderStateName\":\"成功\",\"OrderType\":\"4\",\"OrderTypeName\":\"同步交易订单\",\"PayID\":\"2\",\"PayWayID\":\"1\",\"PayWayName\":\"财付通在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"同步课程\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:39:29.127+08:00\",\"GiveEgg\":\"0\",\"ID\":\"83430389\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:38:33.28+08:00\",\"GiveEgg\":\"0\",\"ID\":\"1206699325\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"},{\"AppID\":\"11\",\"CreateTime\":\"2008-09-05T08:31:54.453+08:00\",\"GiveEgg\":\"0\",\"ID\":\"795858378\",\"OrderState\":\"2\",\"OrderStateName\":\"失败\",\"OrderType\":\"3\",\"OrderTypeName\":\"实物交易订单\",\"PayID\":\"3\",\"PayWayID\":\"1\",\"PayWayName\":\"快钱在线支付\",\"TotalMoney\":\"0.01\",\"TotalNumber\":\"0\",\"TradeSource\":\"商城购物\",\"UserID\":\"668288112\"}],\"results\":{\"totalRecords\":\"6\"}}}";
    static String src2 = "{\"a\"  :    [{\"c\":\"1\"}  , { \"d\":\"2\"}, \"c\", \"d\",\"1\",\"2\"], \"b\":\"d\"}";
    static String src3 = "a	the quick brown fox jumps over the lazy dog.";
    static String src4 = "\"9\\3d$fs冬你我他\"";
    static String src5 = "\"hello world\"";

    private Producer producer = new MonoJsonProducer();

    @Test
    public void test1() throws IOException {

        JsonDict j = (JsonDict) Factory.build(src1, producer);

        String userId = j.getJsonDict("Consume").getJsonList("record")
                .getJsonDict(5).getJsonScalar("UserID").getString();
        Assert.assertEquals("668288112", userId);
    }

    @Test
    public void test2() throws IOException {
        // 冬: 0x2F81A, 194586
        String PLAIN_TEXT = "a	the quick brown fox jumps over the lazy dog.9\\u3d$fs冬你我他";

        JsonScalar jS = producer.produceJsonScalar().set(PLAIN_TEXT);
        JsonList jL = producer.produceJsonList();
        jL.insert(producer.produceJsonScalar().set("vbvb"));
        JsonList jList = producer.produceJsonList();

        jList.insert(producer.produceJsonScalar().set("fffff"));

        JsonDict jHash = producer.produceJsonDict();
        jHash.put("ji", jS);
        jHash.put("dddd", producer.produceJsonScalar().set("77"));
        jHash.put("vvv", jList);

        String code = Factory.serialize(jHash);
        Json j = Factory.build(code, producer);
        Assert.assertEquals(Factory.serialize(jHash),
                Factory.serialize(j));
    }

    @Test
    public void testSerialize() {
        Json j = Factory.build(src1, producer);
        String t = Factory.serialize(j);

        Json j2 = Factory.build(t, producer);
        String t2 = Factory.serialize(j2);
        Assert.assertEquals(t2, t);

        System.out.println(Factory.serializeAsWellFormed(j2, "==="));
    }
}
