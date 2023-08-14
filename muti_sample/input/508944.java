public class WeekView extends CalendarView {
    private static final int CELL_MARGIN = 0;
    public WeekView(CalendarActivity activity) {
        super(activity);
        init();
    }
    private void init() {
        mDrawTextInEventRect = true;
        mNumDays = 7;
        mEventGeometry.setCellMargin(CELL_MARGIN);
    }
}
