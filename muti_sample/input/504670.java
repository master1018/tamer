@TestTargetClass(TimeKeyListener.class)
public class TimeKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public TimeKeyListenerTest(){
        super("com.android.cts.stub", KeyListenerStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mTextView = (TextView) mActivity.findViewById(R.id.keylistener_textview);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "TimeKeyListener",
        args = {}
    )
    public void testConstructor() {
        new TimeKeyListener();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        TimeKeyListener listener1 = TimeKeyListener.getInstance();
        TimeKeyListener listener2 = TimeKeyListener.getInstance();
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAcceptedChars",
        args = {}
    )
    public void testGetAcceptedChars() {
        MyTimeKeyListener timeKeyListener = new MyTimeKeyListener();
        TextMethodUtils.assertEquals(TimeKeyListener.CHARACTERS,
                timeKeyListener.getAcceptedChars());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    public void testGetInputType() {
        TimeKeyListener listener = TimeKeyListener.getInstance();
        int expected = InputType.TYPE_CLASS_DATETIME
                | InputType.TYPE_DATETIME_VARIATION_TIME;
        assertEquals(expected, listener.getInputType());
    }
    public void testTimeKeyListener() {
        final TimeKeyListener timeKeyListener = TimeKeyListener.getInstance();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(timeKeyListener);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("1", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_2);
        assertEquals("12", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_A);
        assertEquals("12a", mTextView.getText().toString());
        int keyCode = TextMethodUtils.getUnacceptedKeyCode(TimeKeyListener.CHARACTERS);
        if (-1 != keyCode) {
            sendKeys(keyCode);
            assertEquals("12a", mTextView.getText().toString());
        }
        sendKeys(KeyEvent.KEYCODE_M);
        assertEquals("12am", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("12am", mTextView.getText().toString());
    }
    private class MyTimeKeyListener extends TimeKeyListener {
        @Override
        protected char[] getAcceptedChars() {
            return super.getAcceptedChars();
        }
    }
}
