@TestTargetClass(DateTimeKeyListener.class)
public class DateTimeKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public DateTimeKeyListenerTest(){
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
        method = "DateTimeKeyListener",
        args = {}
    )
    public void testConstructor() {
        new DateTimeKeyListener();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        DateTimeKeyListener listener1 = DateTimeKeyListener.getInstance();
        DateTimeKeyListener listener2 = DateTimeKeyListener.getInstance();
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
        MyDateTimeKeyListener dataTimeKeyListener = new MyDateTimeKeyListener();
        TextMethodUtils.assertEquals(DateTimeKeyListener.CHARACTERS,
                dataTimeKeyListener.getAcceptedChars());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testGetInputType() {
        DateTimeKeyListener listener = DateTimeKeyListener.getInstance();
        int expected = InputType.TYPE_CLASS_DATETIME
                | InputType.TYPE_DATETIME_VARIATION_NORMAL;
        assertEquals(expected, listener.getInputType());
    }
    public void testDateTimeKeyListener() {
        final DateTimeKeyListener dateTimeKeyListener = DateTimeKeyListener.getInstance();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(dateTimeKeyListener);
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
        int keyCode = TextMethodUtils.getUnacceptedKeyCode(DateTimeKeyListener.CHARACTERS);
        if (-1 != keyCode) {
            sendKeys(keyCode);
            assertEquals("12a", mTextView.getText().toString());
        }
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("12a", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("12a", mTextView.getText().toString());
    }
    private class MyDateTimeKeyListener extends DateTimeKeyListener {
        @Override
        protected char[] getAcceptedChars() {
            return super.getAcceptedChars();
        }
    }
}
