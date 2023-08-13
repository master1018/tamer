public class WeekActivity extends CalendarActivity implements ViewSwitcher.ViewFactory {
    private static final int VIEW_ID = 1;
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.week_activity);
        mSelectedDay = Utils.timeFromIntent(getIntent());
        mViewSwitcher = (ViewSwitcher) findViewById(R.id.switcher);
        mViewSwitcher.setFactory(this);
        mViewSwitcher.getCurrentView().requestFocus();
        mProgressBar = (ProgressBar) findViewById(R.id.progress_circular);
    }
    public View makeView() {
        WeekView wv = new WeekView(this);
        wv.setId(VIEW_ID);
        wv.setLayoutParams(new ViewSwitcher.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        wv.setSelectedDay(mSelectedDay);
        return wv;
    }
    @Override
    protected void onNewIntent(Intent intent) {
        long timeMillis = Utils.timeFromIntentInMillis(intent);
        if (timeMillis > 0) {
            Time time = new Time();
            time.set(timeMillis);
            goTo(time, false);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        CalendarView view1 = (CalendarView) mViewSwitcher.getCurrentView();
        CalendarView view2 = (CalendarView) mViewSwitcher.getNextView();
        SharedPreferences prefs = CalendarPreferenceActivity.getSharedPreferences(this);
        String str = prefs.getString(CalendarPreferenceActivity.KEY_DETAILED_VIEW,
                CalendarPreferenceActivity.DEFAULT_DETAILED_VIEW);
        view1.setDetailedView(str);
        view2.setDetailedView(str);
    }
    @Override
    protected void onPause() {
        super.onPause();
        CalendarView view = (CalendarView) mViewSwitcher.getCurrentView();
        mSelectedDay = view.getSelectedDay();
        Utils.setDefaultView(this, CalendarApplication.WEEK_VIEW_ID);
    }
}
