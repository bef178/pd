package pd.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.time.TimeExtension.MILLISECONDS_PER_MINUTE;

public class Test_SimpleZoneOffset {

    @Test
    public void test() {
        SimpleTimeZone simpleTimeZone = SimpleTimeZone.parse("+0800");
        assertEquals(8 * 60 * MILLISECONDS_PER_MINUTE, simpleTimeZone.getOffsetMilliseconds());
    }
}
