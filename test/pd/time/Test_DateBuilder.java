package pd.time;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;

import pd.time.EasyTime;
import pd.time.TimeUtil.TimeField;
import pd.time.ZoneTimeOffset;

public class Test_DateBuilder {

    @Test
    public void test_toDate2() {
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
}
