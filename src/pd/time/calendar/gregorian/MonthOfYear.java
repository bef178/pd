package pd.time.calendar.gregorian;

public enum MonthOfYear {

    January, February, March, April, May, June, July, August, September, October, November,
    December;

    public int toInt() {
        return ordinal() + 1;
    }
}
