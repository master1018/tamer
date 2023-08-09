public abstract class ImfBaseTestCase<T extends Activity> extends InstrumentationTestCase {
    public final long WAIT_FOR_IME = 5000;
    public final int IME_MIN_HEIGHT = 150;
    public final int IME_MAX_HEIGHT = 300;
    protected InputMethodManager mImm;
    protected T mTargetActivity;
    protected boolean mExpectAutoPop;
    private Class<T> mTargetActivityClass;
    public ImfBaseTestCase(Class<T> activityClass) {
        mTargetActivityClass = activityClass;
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        final String packageName = getInstrumentation().getTargetContext().getPackageName();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        mTargetActivity = launchActivityWithIntent(packageName, mTargetActivityClass, intent);
        int keyboardType = mTargetActivity.getResources().getConfiguration().keyboard;
        mExpectAutoPop = (keyboardType  == Configuration.KEYBOARD_NOKEYS ||
                keyboardType == Configuration.KEYBOARD_UNDEFINED);
        mImm = InputMethodManager.getInstance(mTargetActivity);
        KeyguardManager keyguardManager =
            (KeyguardManager) getInstrumentation().getContext().getSystemService(
                    Context.KEYGUARD_SERVICE);
        keyguardManager.newKeyguardLock("imftest").disableKeyguard();
    }
    public void verifyEditTextAdjustment(final View editText, int rootViewHeight) {
        int[] origLocation = new int[2];
        int[] newLocation = new int[2];
        mImm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
        mTargetActivity.runOnUiThread(new Runnable() {
            public void run() {
                editText.requestFocus();
            }
        });
        editText.getLocationOnScreen(origLocation);
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        editText.getLocationOnScreen(newLocation);
        long timeoutTime = SystemClock.uptimeMillis() + WAIT_FOR_IME;
        while (newLocation[1] > rootViewHeight - IME_MIN_HEIGHT && SystemClock.uptimeMillis() < timeoutTime) {
            editText.getLocationOnScreen(newLocation);
            pause(100);
        }
        assertTrue(newLocation[1] <= rootViewHeight - IME_MIN_HEIGHT);
        mImm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }
    public void destructiveCheckImeInitialState(View rootView, View servedView) {
        int windowSoftInputMode = mTargetActivity.getWindow().getAttributes().softInputMode;
        int adjustMode = windowSoftInputMode & WindowManager.LayoutParams.SOFT_INPUT_MASK_ADJUST;
        if (mExpectAutoPop && adjustMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) {
            assertTrue(destructiveCheckImeUp(rootView, servedView));
        } else {
            assertFalse(destructiveCheckImeUp(rootView, servedView));
        }
    }
    public boolean destructiveCheckImeUp(View rootView, View servedView) {
        int origHeight;
        int newHeight;
        origHeight = rootView.getHeight();
        mImm.hideSoftInputFromWindow(servedView.getWindowToken(), 0);
        newHeight = rootView.getHeight();
        long timeoutTime = SystemClock.uptimeMillis() + WAIT_FOR_IME;
        while (Math.abs(newHeight - origHeight) < IME_MIN_HEIGHT && SystemClock.uptimeMillis() < timeoutTime) {
            newHeight = rootView.getHeight();
        }
        return (Math.abs(origHeight - newHeight) >= IME_MIN_HEIGHT);
    }
    void pause(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }
}
