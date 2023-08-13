@TestTargetClass(DateKeyListener.class)
public class DateKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public DateKeyListenerTest(){
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
        method = "DateKeyListener",
        args = {}
    )
    public void testConstructor() {
        new DateKeyListener();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        DateKeyListener listener1 = DateKeyListener.getInstance();
        DateKeyListener listener2 = DateKeyListener.getInstance();
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
        MyDataKeyListener dataKeyListener = new MyDataKeyListener();
        TextMethodUtils.assertEquals(DateKeyListener.CHARACTERS,
                dataKeyListener.getAcceptedChars());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testGetInputType() {
        MyDataKeyListener dataKeyListener = new MyDataKeyListener();
        int expected = InputType.TYPE_CLASS_DATETIME
                | InputType.TYPE_DATETIME_VARIATION_DATE;
        assertEquals(expected, dataKeyListener.getInputType());
    }
    public void testDateTimeKeyListener() {
        final DateKeyListener dateKeyListener = DateKeyListener.getInstance();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(dateKeyListener);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("1", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_2);
        assertEquals("12", mTextView.getText().toString());
        int keyCode = TextMethodUtils.getUnacceptedKeyCode(DateKeyListener.CHARACTERS);
        if (-1 != keyCode) {
            sendKeys(keyCode);
            assertEquals("12", mTextView.getText().toString());
        }
        sendKeys(KeyEvent.KEYCODE_MINUS);
        assertEquals("12-", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_SLASH);
        assertEquals("12-/", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("12-/", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_SLASH);
        assertEquals("12-/", mTextView.getText().toString());
    }
    private class MyDataKeyListener extends DateKeyListener {
        @Override
        protected char[] getAcceptedChars() {
            return super.getAcceptedChars();
        }
    }
}
