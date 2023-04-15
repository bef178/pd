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
        assertEquals(4, components[TimeExtension.INDEX_DAY_OF_WEEK]);
    }

    @Test
    public void test_findTimeComponents_20210606() {
        int[] components = TimeExtension.findTimeComponents(1622947550000L);
        assertEquals(2021, components[TimeExtension.INDEX_YEAR_OF_TIME]);
        assertEquals(6, components[TimeExtension.INDEX_MONTH_OF_YEAR] + 1);
        assertEquals(6, components[TimeExtension.INDEX_DAY_OF_MONTH] + 1);
        assertEquals(2, components[TimeExtension.INDEX_HOUR_OF_DAY]);
        assertEquals(45, components[TimeExtension.INDEX_MINUTE_OF_HOUR]);
        assertEquals(50, components[TimeExtension.INDEX_SECOND_OF_MINUTE]);
        assertEquals(0, components[TimeExtension.INDEX_MILLISECOND_OF_SECOND]);
        assertEquals(0, components[TimeExtension.INDEX_DAY_OF_WEEK]);
    }

    @Test
    public void test_findTimeComponents() {
        test_findTimeComponents(-1741415316);
        test_findTimeComponents(1594910582);
        test_findTimeComponents(-614716713);
        test_findTimeComponents(647727371);

        Random random = new Random(System.nanoTime());
        for (int i = 0; i < 50000000; i++) {
            test_findTimeComponents(random.nextInt());
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
}
