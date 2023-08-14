public class MusicPlayerLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "MusicPlayerLaunchPerformance";
    public MusicPlayerLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.music.MusicBrowserActivity");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}
