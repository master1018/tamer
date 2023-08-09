public class DayActivity extends CalendarActivity implements ViewSwitcher.ViewFactory {
    private static final int VIEW_ID = 1;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.day_activity);
        mSelectedDay = Utils.timeFromIntent(getIntent());
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.switcher);
        mViewSwitcher.setFactory(this);
        mViewSwitcher.getCurrentView().requestFocus();
        mProgressBar = (ProgressBar) findViewById(R.id.progress_circular);
    }
    public View makeView() {
        DayView view = new DayView(this);
        view.setId(VIEW_ID);
        view.setLayoutParams(new ViewSwitcher.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        view.setSelectedDay(mSelectedDay);
        return view;
    }
    @Override
    protected void onPause() {
        super.onPause();
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        mSelectedDay = view.getSelectedDay();
        Utils.setDefaultView(this, CalendarApplication.DAY_VIEW_ID);
    }
}
