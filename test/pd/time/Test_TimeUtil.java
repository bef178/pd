package pd.time;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static pd.time.TimeUtil.totalDays;
import static pd.time.TimeUtil.toDayOfYear;

import java.time.LocalDate;
import java.util.Random;

import org.junit.Test;

public class Test_TimeUtil {

    @Test
    public void test_getDayOfYear() {
        Random rand = new Random();
        for (int i = 0; i < 50000000; i++) {
            long daysSinceEpoch = rand.nextInt();
            LocalDate d = LocalDate.ofEpochDay(daysSinceEpoch);
            long actual = toDayOfYear(d.getYear(),
                    d.getMonthValue() - 1,
                    d.getDayOfMonth() - 1);
            assertEquals(d.getDayOfYear() - 1, actual);
        }
    }

    @Test
    public void test_getDays() {
        assertEquals(0, totalDays(1970, 0));
        assertEquals(365 * 1, totalDays(1971, 0));
        assertEquals(365 * 2, totalDays(1972, 0));
        assertEquals(365 * 3 + 1, totalDays(1973, 0));
        assertEquals(365 * 4 + 1, totalDays(1974, 0));
        assertEquals(365 * 5 + 1, totalDays(1975, 0));
        assertEquals(365 * 6 + 1, totalDays(1976, 0));
        assertEquals(365 * 7 + 2, totalDays(1977, 0));
        assertEquals(365 * 8 + 2, totalDays(1978, 0));

        assertEquals(42734, totalDays(2087, 0));
        assertEquals(43099, totalDays(2088, 0));
        assertEquals(43465, totalDays(2089, 0));
        assertEquals(43830, totalDays(2090, 0));
        assertEquals(44195, totalDays(2091, 0));

        Random rand = new Random();
        for (int i = 0; i < 50000000; i++) {
            int year = rand.nextInt(LocalDate.MAX.getYear());
            long expected = LocalDate.ofYearDay(year, 1).toEpochDay();
            long actual = totalDays(year, 0);
            assertEquals(expected, actual);
        }
    }
}
