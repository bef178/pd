package pd.time;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import pd.time.EasyTime;
import pd.time.MonthOfYear;
import pd.time.ZoneTimeOffset;

public class Test_EasyTime {

    @Test
    public void test_toString() {
        long milliseconds = 1556449013818L;

        EasyTime date = EasyTime.builder()
                .setLocalTotalMilliseconds(milliseconds)
                .setTimeZone(ZoneTimeOffset.UTC)
                .build();
        assertEquals("2019-04-28 10:56:53.818 +0000", date.toString());

        date = date.rebase(ZoneTimeOffset.fromString("+0800"));
        assertEquals("2019-04-28 18:56:53.818 +0800", date.toString());

        date = EasyTime.builder().setLocalDatePart(2019, MonthOfYear.April, 28)
                .setLocalTimePart(18, 56, 53, 818)
                .setTimeZone(ZoneTimeOffset.fromString("+0800")).build()
                .rebase(ZoneTimeOffset.UTC);
        assertEquals("2019-04-28 10:56:53.818 +0000", date.toString());
        assertEquals(milliseconds, date.getMillisecondsSinceEpoch());
    }
}
