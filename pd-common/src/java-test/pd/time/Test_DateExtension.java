package pd.time;

import java.time.LocalDate;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_DateExtension {

    @Test
    public void test_findDatePart_random50m() {
        Random random = new Random(System.nanoTime());
        long minDays = LocalDate.MIN.toEpochDay();
        long maxDays = LocalDate.MAX.toEpochDay();
        for (int i = 0; i < 50_000_000; i++) {
            long days = (long) (random.nextDouble() * (maxDays - minDays) + minDays);
            test_findDatePart(days);
        }
    }

    private void test_findDatePart(long daysSinceEpoch) {
        LocalDate expected = LocalDate.ofEpochDay(daysSinceEpoch);
        int[] components = TimeExtension.createTimeComponents();
        DateExtension.findDatePart(daysSinceEpoch, components);
        assertEquals(expected.getYear(), components[TimeExtension.INDEX_YEAR_OF_TIME]);
        assertEquals(expected.getMonthValue() - 1, components[TimeExtension.INDEX_MONTH_OF_YEAR]);
        assertEquals(expected.getDayOfMonth() - 1, components[TimeExtension.INDEX_DAY_OF_MONTH]);
        assertEquals((expected.getDayOfWeek().ordinal() + 1) % 7, components[TimeExtension.INDEX_DAY_OF_WEEK]);
    }

    @Test
    public void test_toDayOfYear_random50m() {
        Random random = new Random(System.nanoTime());
        for (int i = 0; i < 50000000; i++) {
            long daysSinceEpoch = random.nextLong();
            if (daysSinceEpoch < LocalDate.MIN.toEpochDay() || daysSinceEpoch >= LocalDate.MAX.toEpochDay()) {
                continue;
            }
            LocalDate d = LocalDate.ofEpochDay(daysSinceEpoch);
            long expected = d.getDayOfYear() - 1;
            long actual = DateExtension.toDayOfYear(d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth() - 1);
            assertEquals(expected, actual);
        }
    }

    public void test_toDaysSinceEpoch_sample() {
        assertEquals(0, DateExtension.toDaysSinceEpoch(1970, 0));
        assertEquals(365 * 1, DateExtension.toDaysSinceEpoch(1971, 0));
        assertEquals(365 * 2, DateExtension.toDaysSinceEpoch(1972, 0));
        assertEquals(365 * 3 + 1, DateExtension.toDaysSinceEpoch(1973, 0));
        assertEquals(365 * 4 + 1, DateExtension.toDaysSinceEpoch(1974, 0));
        assertEquals(365 * 5 + 1, DateExtension.toDaysSinceEpoch(1975, 0));
        assertEquals(365 * 6 + 1, DateExtension.toDaysSinceEpoch(1976, 0));
        assertEquals(365 * 7 + 2, DateExtension.toDaysSinceEpoch(1977, 0));
        assertEquals(365 * 8 + 2, DateExtension.toDaysSinceEpoch(1978, 0));
        assertEquals(42734, DateExtension.toDaysSinceEpoch(2087, 0));
        assertEquals(43099, DateExtension.toDaysSinceEpoch(2088, 0));
        assertEquals(43465, DateExtension.toDaysSinceEpoch(2089, 0));
        assertEquals(43830, DateExtension.toDaysSinceEpoch(2090, 0));
        assertEquals(44195, DateExtension.toDaysSinceEpoch(2091, 0));
    }

    @Test
    public void test_toDaysSinceEpoch_random50m() {
        Random random = new Random();
        for (int i = 0; i < 50000000; i++) {
            int year = random.nextInt(LocalDate.MAX.getYear());
            long expected = LocalDate.ofYearDay(year, 1).toEpochDay();
            long actual = DateExtension.toDaysSinceEpoch(year, 0);
            assertEquals(expected, actual);
        }
    }
}
