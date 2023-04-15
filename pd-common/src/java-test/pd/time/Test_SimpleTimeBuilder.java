package pd.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_SimpleTimeBuilder {

    @Test
    public void test_parse() {
        SimpleTime time = SimpleTime.builder().parse("2019-05-12 22:54:00.000 +0800").build();
        assertEquals(2019, time.getYear());
        assertEquals(MonthOfYear.May, time.getMonth());
        assertEquals(12, time.getDay());
        assertEquals(22, time.getHour());
        assertEquals(54, time.getMinute());
        assertEquals(0, time.getSecond());
        assertEquals(0, time.getMillisecond());
        assertEquals(SimpleTimeZone.parse("+0800"), time.getTimeZone());
    }

    @Test
    public void test_parse_utc() {
        SimpleTime time = SimpleTime.builder().parse("2019-04-28T10:56:53.818Z").build();
        assertEquals(2019, time.getYear());
        assertEquals(MonthOfYear.April, time.getMonth());
        assertEquals(28, time.getDay());
        assertEquals(10, time.getHour());
        assertEquals(56, time.getMinute());
        assertEquals(53, time.getSecond());
        assertEquals(818, time.getMillisecond());
        assertEquals(SimpleTimeZone.UTC, time.getTimeZone());
    }

    @Test
    public void test_parse_cst() {
        SimpleTime time = SimpleTime.builder().parse("2019-04-28T10:56:53.818+0800").build();
        assertEquals(2019, time.getYear());
        assertEquals(MonthOfYear.April, time.getMonth());
        assertEquals(28, time.getDay());
        assertEquals(10, time.getHour());
        assertEquals(56, time.getMinute());
        assertEquals(53, time.getSecond());
        assertEquals(818, time.getMillisecond());
        assertEquals(SimpleTimeZone.parse("+0800"), time.getTimeZone());
    }
}
