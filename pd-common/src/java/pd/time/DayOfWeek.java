package pd.time;

public enum DayOfWeek {

    Sunday, Monday, Tuesday, Wednesday, Thursday, Friday, Saturday;

    private static final DayOfWeek[] values = DayOfWeek.values();

    public static final DayOfWeek fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < 7) {
            return values[ordinal];
        }
        return null;
    }
}
