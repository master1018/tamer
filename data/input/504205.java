public class MonthDisplayHelper {
    private final int mWeekStartDay;
    private Calendar mCalendar;
    private int mNumDaysInMonth;
    private int mNumDaysInPrevMonth;
    private int mOffset;
    public MonthDisplayHelper(int year, int month, int weekStartDay) {
        if (weekStartDay < Calendar.SUNDAY || weekStartDay > Calendar.SATURDAY) {
            throw new IllegalArgumentException();
        }
        mWeekStartDay = weekStartDay;
        mCalendar = Calendar.getInstance();
        mCalendar.set(Calendar.YEAR, year);
        mCalendar.set(Calendar.MONTH, month);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mCalendar.set(Calendar.MINUTE, 0);
        mCalendar.set(Calendar.SECOND, 0);
        mCalendar.getTimeInMillis();
        recalculate();
    }
    public MonthDisplayHelper(int year, int month) {
        this(year, month, Calendar.SUNDAY);
    }
    public int getYear() {
        return mCalendar.get(Calendar.YEAR);
    }
    public int getMonth() {
        return mCalendar.get(Calendar.MONTH);
    }
    public int getWeekStartDay() {
        return mWeekStartDay;
    }
    public int getFirstDayOfMonth() {
        return mCalendar.get(Calendar.DAY_OF_WEEK);
    }
    public int getNumberOfDaysInMonth() {
        return mNumDaysInMonth;
    }
    public int getOffset() {
        return mOffset;
    }
    public int[] getDigitsForRow(int row) {
        if (row < 0 || row > 5) {
            throw new IllegalArgumentException("row " + row
                    + " out of range (0-5)");
        }
        int [] result = new int[7];
        for (int column = 0; column < 7; column++) {
            result[column] = getDayAt(row, column);
        }
        return result;
    }
    public int getDayAt(int row, int column) {
        if (row == 0 && column < mOffset) {
            return mNumDaysInPrevMonth + column - mOffset + 1;
        }
        int day = 7 * row + column - mOffset + 1;
        return (day > mNumDaysInMonth) ?
                day - mNumDaysInMonth : day;
    }
    public int getRowOf(int day) {
        return (day + mOffset - 1) / 7;
    }
    public int getColumnOf(int day) {
        return (day + mOffset - 1) % 7;
    }
    public void previousMonth() {
        mCalendar.add(Calendar.MONTH, -1);
        recalculate();
    }
    public void nextMonth() {
        mCalendar.add(Calendar.MONTH, 1);
        recalculate();
    }
    public boolean isWithinCurrentMonth(int row, int column) {
        if (row < 0 || column < 0 || row > 5 || column > 6) {
            return false;
        }
        if (row == 0 && column < mOffset) {
            return false;
        }
        int day = 7 * row + column - mOffset + 1;
        if (day > mNumDaysInMonth) {
            return false;
        }
        return true;
    }
    private void recalculate() {
        mNumDaysInMonth = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        mCalendar.add(Calendar.MONTH, -1);
        mNumDaysInPrevMonth = mCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        mCalendar.add(Calendar.MONTH, 1);
        int firstDayOfMonth = getFirstDayOfMonth();
        int offset = firstDayOfMonth - mWeekStartDay;
        if (offset < 0) {
            offset += 7;
        }
        mOffset = offset;
    }
}
