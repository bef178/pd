package pd.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import pd.time.Ctime.TimeField;

public class Test_EasyTime {

    @Test
    public void test_fillByString() {
        EasyTime time = EasyTime.builder().fillByString("2019-05-12 22:54:00.000 +0800").build();
        assertEquals(2019, time.getFieldValue(TimeField.YEAR));
        assertEquals(5, time.getFieldValue(TimeField.MONTH_OF_YEAR));
        assertEquals(12, time.getFieldValue(TimeField.DAY_OF_MONTH));
        assertEquals(22, time.getFieldValue(TimeField.HH));
        assertEquals(54, time.getFieldValue(TimeField.MM));
        assertEquals(0, time.getFieldValue(TimeField.SS));
        assertEquals(0, time.getFieldValue(TimeField.SSS));
        assertEquals(ZoneTimeOffset.fromString("+0800"), time.getTimeZone());
    }

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
