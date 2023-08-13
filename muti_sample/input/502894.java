public class DayOfMonthCursor extends MonthDisplayHelper {
    private int mRow;
    private int mColumn;
    public DayOfMonthCursor(int year, int month, int dayOfMonth, int weekStartDay) {
        super(year, month, weekStartDay);
        mRow = getRowOf(dayOfMonth);
        mColumn = getColumnOf(dayOfMonth);
    }
    public int getSelectedRow() {
        return mRow;
    }
    public int getSelectedColumn() {
        return mColumn;
    }
    public void setSelectedRowColumn(int row, int col) {
        mRow = row;
        mColumn = col;
    }
    public int getSelectedDayOfMonth() {
        return getDayAt(mRow, mColumn);
    }
    public int getSelectedMonthOffset() {
        if (isWithinCurrentMonth(mRow, mColumn)) {
            return 0;
        }
        if (mRow == 0) {
            return -1;
        }
        return 1;
    }
    public void setSelectedDayOfMonth(int dayOfMonth) {
        mRow = getRowOf(dayOfMonth);
        mColumn = getColumnOf(dayOfMonth);
    }
    public boolean isSelected(int row, int column) {
        return (mRow == row) && (mColumn == column);
    }
    public boolean up() {
        if (isWithinCurrentMonth(mRow - 1, mColumn)) {
            mRow--;
            return false;
        }
        previousMonth();
        mRow = 5;
        while(!isWithinCurrentMonth(mRow, mColumn)) {
            mRow--;
        }
        return true;
    }
    public boolean down() {
        if (isWithinCurrentMonth(mRow + 1, mColumn)) {
            mRow++;
            return false;
        }
        nextMonth();
        mRow = 0;
        while (!isWithinCurrentMonth(mRow, mColumn)) {
            mRow++;
        }
        return true;
    }
    public boolean left() {
        if (mColumn == 0) {
            mRow--;
            mColumn = 6;
        } else {
            mColumn--;
        }
        if (isWithinCurrentMonth(mRow, mColumn)) {
            return false;
        }
        previousMonth();
        int lastDay = getNumberOfDaysInMonth();
        mRow = getRowOf(lastDay);
        mColumn = getColumnOf(lastDay);
        return true;
    }
    public boolean right() {
        if (mColumn == 6) {
            mRow++;
            mColumn = 0;
        } else {
            mColumn++;
        }
        if (isWithinCurrentMonth(mRow, mColumn)) {
            return false;
        }
        nextMonth();
        mRow = 0;
        mColumn = 0;
        while (!isWithinCurrentMonth(mRow, mColumn)) {
            mColumn++;
        }
        return true;
    }
}
