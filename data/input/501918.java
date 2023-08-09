public class LocalActivityManagerStubActivity extends Activity{
    public static boolean sIsOnResumeCalled;
    public static boolean sIsOnStopCalled;
    public static boolean sIsOnPauseCalled;
    public static boolean sIsOnDestroyCalled;
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }
    @Override
    public void onResume() {
        super.onResume();
        sIsOnResumeCalled = true;
    }
    @Override
    protected void onStop() {
        super.onStop();
        sIsOnStopCalled = true;
    }
    @Override
    protected void onPause() {
        super.onPause();
        sIsOnPauseCalled = true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sIsOnDestroyCalled = true;
    }
}
