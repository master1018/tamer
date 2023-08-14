public class NotePadLaunchPerformance extends LaunchPerformanceBase {
    public static final String LOG_TAG = "NotePadLaunchPerformance";
    public NotePadLaunchPerformance() {
        super();
    }
    @Override
    public void onCreate(Bundle arguments) {
        super.onCreate(arguments);
        mIntent.setClassName(getTargetContext(), "com.android.notepad.NotesList");
        start();
    }
    @Override
    public void onStart() {
        super.onStart();
        LaunchApp();
        finish(Activity.RESULT_OK, mResults);
    }
}
