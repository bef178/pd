package pd.time;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_TimeExtension {

    @Test
    public void test_findTimeComponents_epoch() {
        int[] components = TimeExtension.findTimeComponents(0);
        assertEquals(1970, components[TimeExtension.INDEX_YEAR_OF_TIME]);
        assertEquals(0, components[TimeExtension.INDEX_MONTH_OF_YEAR]);
        assertEquals(0, components[TimeExtension.INDEX_DAY_OF_MONTH]);
        assertEquals(0, components[TimeExtension.INDEX_HOUR_OF_DAY]);
        assertEquals(0, components[TimeExtension.INDEX_MINUTE_OF_HOUR]);
        assertEquals(0, components[TimeExtension.INDEX_SECOND_OF_MINUTE]);
        assertEquals(0, components[TimeExtension.INDEX_MILLISECOND_OF_SECOND]);
        assertEquals(0, components[TimeExtension.INDEX_WEEK_OF_YEAR]);
        assertEquals(4, components[TimeExtension.INDEX_DAY_OF_WEEK]);
    }

    @Test
    public void test_findTimeComponents_sample() {
        test_findTimeComponents(654280699561200000L);
        test_findTimeComponents(654280699564800000L);
    }

    @Test
    public void test_findTimeComponents_random50m() {
        Random random = new Random(System.nanoTime());
        for (int i = 0; i < 50_000_000; i++) {
            test_findTimeComponents(random.nextLong());
        }
    }

    private void test_findTimeComponents(long milliseconds) {
        int[] components = TimeExtension.findTimeComponents(milliseconds);
        OffsetDateTime expected = Instant.ofEpochMilli(milliseconds).atOffset(ZoneOffset.UTC);
        assertEquals(expected.getYear(), components[TimeExtension.INDEX_YEAR_OF_TIME]);
        assertEquals(expected.getMonthValue() - 1, components[TimeExtension.INDEX_MONTH_OF_YEAR]);
        assertEquals(expected.getDayOfMonth() - 1, components[TimeExtension.INDEX_DAY_OF_MONTH]);
        assertEquals(expected.getHour(), components[TimeExtension.INDEX_HOUR_OF_DAY]);
        assertEquals(expected.getMinute(), components[TimeExtension.INDEX_MINUTE_OF_HOUR]);
        assertEquals(expected.getSecond(), components[TimeExtension.INDEX_SECOND_OF_MINUTE]);
        assertEquals(expected.getNano() / 1000000, components[TimeExtension.INDEX_MILLISECOND_OF_SECOND]);
        assertEquals((expected.getDayOfWeek().ordinal() + 1) % 7, components[TimeExtension.INDEX_DAY_OF_WEEK]);
    }

    @Test
    public void test_toUtcString() {
        long milliseconds = 1622947550000L;
        assertEquals("2021-06-06T02:45:50.000Z", TimeExtension.toUtcString(milliseconds));
    }
}
