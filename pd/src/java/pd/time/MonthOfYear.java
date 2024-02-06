package pd.time;

public enum MonthOfYear {

    January, February, March, April, May, June,
    July, August, September, October, November, December;

    private static final MonthOfYear[] values = MonthOfYear.values();

    public static final MonthOfYear fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return null;
    }
}
