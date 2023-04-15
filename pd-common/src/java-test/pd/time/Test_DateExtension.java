package pd.time;

import java.time.LocalDate;
import java.util.Random;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Test_DateExtension {

    @Test
    public void test_toDayOfYear() {
        Random random = new Random(System.nanoTime());
        for (int i = 0; i < 50000000; i++) {
            long daysSinceEpoch = random.nextInt();
            LocalDate d = LocalDate.ofEpochDay(daysSinceEpoch);
            long expected = d.getDayOfYear() - 1;
            long actual = DateExtension.toDayOfYear(d.getYear(), d.getMonthValue() - 1, d.getDayOfMonth() - 1);
            assertEquals(expected, actual);
        }
    }

    @Test
    public void test_toDaysSinceEpoch() {
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

        Random rand = new Random();
        for (int i = 0; i < 50000000; i++) {
            int year = rand.nextInt(LocalDate.MAX.getYear());
            long expected = LocalDate.ofYearDay(year, 1).toEpochDay();
            long actual = DateExtension.toDaysSinceEpoch(year, 0);
            assertEquals(expected, actual);
        }
    }
}
