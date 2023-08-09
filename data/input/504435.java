public class CalendarLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "CalendarLaunchPerformance";
    public CalendarLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.calendar.LaunchActivity");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}
