package pd.time.calendar.gregorian;

public enum DayOfWeek {

    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;

    private static final DayOfWeek[] values = DayOfWeek.values();

    public static final DayOfWeek from(int ordinal) {
        if (ordinal >= 0 && ordinal < 7) {
            return values[ordinal];
        }
        return null;
    }

    public DayOfWeek addDays(long n) {
        n = (n + this.ordinal()) % 7;
        if (n < 0) {
            n += 7;
        }
        return from((int) n);
    }

    public int toInt() {
        return ordinal();
    }
}
