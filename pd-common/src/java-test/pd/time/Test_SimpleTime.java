package pd.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class Test_SimpleTime {

    @Test
    public void test_toString() {
        long milliseconds = 1556449013818L;

        SimpleTime date = SimpleTime.builder()
                .setLocalMilliseconds(milliseconds)
                .setTimeZone(SimpleTimeZone.UTC)
                .build();
        assertEquals("2019-04-28T10:56:53.818Z", date.toString());

        date = date.rebase(SimpleTimeZone.parse("+0800"));
        assertEquals("2019-04-28T18:56:53.818+0800", date.toString());

        date = SimpleTime.builder().setLocalDatePart(2019, MonthOfYear.April, 28)
                .setLocalTimePart(18, 56, 53, 818)
                .setTimeZone(SimpleTimeZone.parse("+0800")).build()
                .rebase(SimpleTimeZone.UTC);
        assertEquals("2019-04-28T10:56:53.818Z", date.toString());
        assertEquals(milliseconds, date.getMillisecondsSinceEpoch());
    }
}
