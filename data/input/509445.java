public class EventGeometry {
    private int mCellMargin = 0;
    private float mMinuteHeight;
    private float mHourGap;
    private float mMinEventHeight;
    void setCellMargin(int cellMargin) {
        mCellMargin = cellMargin;
    }
    void setHourGap(float gap) {
        mHourGap = gap;
    }
    void setMinEventHeight(float height) {
        mMinEventHeight = height;
    }
    void setHourHeight(float height) {
        mMinuteHeight = height / 60.0f;
    }
    boolean computeEventRect(int date, int left, int top, int cellWidth, Event event) {
        if (event.allDay) {
            return false;
        }
        float cellMinuteHeight = mMinuteHeight;
        int startDay = event.startDay;
        int endDay = event.endDay;
        if (startDay > date || endDay < date) {
            return false;
        }
        int startTime = event.startTime;
        int endTime = event.endTime;
        if (startDay < date) {
            startTime = 0;
        }
        if (endDay > date) {
            endTime = CalendarView.MINUTES_PER_DAY;
        }
        int col = event.getColumn();
        int maxCols = event.getMaxColumns();
        int startHour = startTime / 60;
        int endHour = endTime / 60;
        if (endHour * 60 == endTime)
            endHour -= 1;
        event.top = top;
        event.top += (int) (startTime * cellMinuteHeight);
        event.top += startHour * mHourGap;
        event.bottom = top;
        event.bottom += (int) (endTime * cellMinuteHeight);
        event.bottom += endHour * mHourGap;
        if (event.bottom < event.top + mMinEventHeight) {
            event.bottom = event.top + mMinEventHeight;
        }
        float colWidth = (float) (cellWidth - 2 * mCellMargin) / (float) maxCols;
        event.left = left + mCellMargin + col * colWidth;
        event.right = event.left + colWidth;
        return true;
    }
    boolean eventIntersectsSelection(Event event, Rect selection) {
        if (event.left < selection.right && event.right >= selection.left
                && event.top < selection.bottom && event.bottom >= selection.top) {
            return true;
        }
        return false;
    }
    float pointToEvent(float x, float y, Event event) {
        float left = event.left;
        float right = event.right;
        float top = event.top;
        float bottom = event.bottom;
        if (x >= left) {
            if (x <= right) {
                if (y >= top) {
                    if (y <= bottom) {
                        return 0f;
                    }
                    return y - bottom;
                }
                return top - y;
            }
            float dx = x - right;
            if (y < top) {
                float dy = top - y;
                return (float) Math.sqrt(dx * dx + dy * dy);
            }
            if (y > bottom) {
                float dy = y - bottom;
                return (float) Math.sqrt(dx * dx + dy * dy);
            }
            return dx;
        }
        float dx = left - x;
        if (y < top) {
            float dy = top - y;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
        if (y > bottom) {
            float dy = y - bottom;
            return (float) Math.sqrt(dx * dx + dy * dy);
        }
        return dx;
    }
}
