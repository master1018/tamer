public class AutoCompleteTextViewCallbacks
        extends ActivityInstrumentationTestCase2<AutoCompleteTextViewSimple> {
    private static final int WAIT_TIME = 200;
    public AutoCompleteTextViewCallbacks() {
        super("com.android.frameworks.coretests", AutoCompleteTextViewSimple.class);
    }
    @FlakyTest(tolerance=3)
    public void testPopupNoSelection() throws Exception {
        AutoCompleteTextViewSimple theActivity = getActivity();
        AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
        instrumentation.waitForIdleSync();
        Thread.sleep(WAIT_TIME);
        assertFalse("onItemClick should not be called", theActivity.mItemClickCalled);
        assertFalse("onItemSelected should not be called", theActivity.mItemSelectedCalled);
    }
    @FlakyTest(tolerance=3)
    public void testPopupEnterSelection() throws Exception {
        final AutoCompleteTextViewSimple theActivity = getActivity();
        AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
        textView.post(new Runnable() {
            public void run() {
                theActivity.resetItemListeners();
            }
        });
        sendKeys("DPAD_DOWN");
        instrumentation.waitForIdleSync();
        Thread.sleep(WAIT_TIME);
        assertFalse("onItemClick should not be called", theActivity.mItemClickCalled);
        assertTrue("onItemSelected should be called", theActivity.mItemSelectedCalled);
        assertEquals("onItemSelected position", 0, theActivity.mItemSelectedPosition);
        assertFalse("onNothingSelected should not be called", theActivity.mNothingSelectedCalled);
        textView.post(new Runnable() {
            public void run() {
                theActivity.resetItemListeners();
            }
        });
        sendKeys("DPAD_DOWN");
        instrumentation.waitForIdleSync();        
        Thread.sleep(WAIT_TIME);
        assertFalse("onItemClick should not be called", theActivity.mItemClickCalled);
        assertTrue("onItemSelected should be called", theActivity.mItemSelectedCalled);
        assertEquals("onItemSelected position", 1, theActivity.mItemSelectedPosition);
        assertFalse("onNothingSelected should not be called", theActivity.mNothingSelectedCalled);
    }
    @FlakyTest(tolerance=3)
    public void testPopupLeaveSelection() {
        final AutoCompleteTextViewSimple theActivity = getActivity();
        AutoCompleteTextView textView = theActivity.getTextView();
        final Instrumentation instrumentation = getInstrumentation();
        textView.requestFocus();
        instrumentation.waitForIdleSync();
        sendKeys("A");
        instrumentation.waitForIdleSync();
        sendKeys("DPAD_DOWN");
        instrumentation.waitForIdleSync();
        textView.post(new Runnable() {
            public void run() {
                theActivity.resetItemListeners();
            }
        });
        sendKeys("DPAD_UP");
        instrumentation.waitForIdleSync();
        assertFalse("onItemClick should not be called", theActivity.mItemClickCalled);
        assertFalse("onItemSelected should not be called", theActivity.mItemSelectedCalled);
        assertTrue("onNothingSelected should be called", theActivity.mNothingSelectedCalled);
    }
}
