package redpoint.time;

public enum DayOfWeek {

    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;

    private static final DayOfWeek[] values = DayOfWeek.values();

    public static final DayOfWeek fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < 7) {
            return values[ordinal];
        }
        return null;
    }

    public DayOfWeek addDays(long days) {
        int dayOfWeek = TimeUtil.daysToDayOfWeek(days);
        return fromOrdinal(dayOfWeek);
    }
}
