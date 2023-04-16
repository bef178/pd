package pd.time;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.time.TimeExtension.MILLISECONDS_PER_MINUTE;

public class Test_SimpleTimeZone {

    @Test
    public void test_parse() {
        SimpleTimeZone simpleTimeZone = SimpleTimeZone.parse("+0800");
        assertEquals(8 * 60 * MILLISECONDS_PER_MINUTE, simpleTimeZone.getOffsetMilliseconds());
    }
}
