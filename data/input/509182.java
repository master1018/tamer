@TestTargetClass(BaseKeyListener.class)
public class BaseKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private static final CharSequence TEST_STRING = "123456";
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public BaseKeyListenerTest(){
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
        method = "backspace",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "1. when there is no any selections, " +
            "an IndexOutOfBoundsException occurs. " +
            "2. ALT+DEL does not delete everything where there is a selection, " +
            "javadoc does not explain this situation")
    public void testBackspace() {
        Editable content;
        final MockBaseKeyListener baseKeyListener = new MockBaseKeyListener();
        KeyEvent delKeyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL);
        content = Editable.Factory.getInstance().newEditable(TEST_STRING);
        Selection.setSelection(content, 0, 0);
        baseKeyListener.backspace(mTextView, content, KeyEvent.KEYCODE_DEL, delKeyEvent);
        assertEquals("123456", content.toString());
        content = Editable.Factory.getInstance().newEditable(TEST_STRING);
        Selection.setSelection(content, 0, 3);
        baseKeyListener.backspace(mTextView, content, KeyEvent.KEYCODE_DEL, delKeyEvent);
        assertEquals("456", content.toString());
        content = Editable.Factory.getInstance().newEditable(TEST_STRING);
        try {
            baseKeyListener.backspace(mTextView, content, KeyEvent.KEYCODE_DEL,delKeyEvent);
            fail("did not throw IndexOutOfBoundsException when there is no selections");
        } catch (IndexOutOfBoundsException e) {
        }
        final String str = "123456";
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.setKeyListener(baseKeyListener);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 1, 1);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("23456", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 1, 3);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("1456", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_ALT_LEFT);
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 2, 4);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_ALT_LEFT);
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("1256", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 1, 1);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals(str, mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals(str, mTextView.getText().toString());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {View.class, Editable.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyOther",
            args = {View.class, Editable.class, KeyEvent.class}
        )
    })
    @ToBeFixed(bug = "1731439", explanation = "onKeyOther doesn't inserts the" +
            " event's text into content.")
    public void testPressKey() {
        final CharSequence str = "123456";
        final MockBaseKeyListener baseKeyListener = new MockBaseKeyListener();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText(str, BufferType.EDITABLE);
                mTextView.setKeyListener(baseKeyListener);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("123456", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_0);
        assertEquals("123456", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Selection.setSelection((Editable) mTextView.getText(), 1, 2);
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_DEL);
        assertEquals("13456", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                Selection.setSelection((Editable) mTextView.getText(), 2, 2);
            }
        });
        mInstrumentation.waitForIdleSync();
        KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(), "abcd",
                KeyCharacterMap.BUILT_IN_KEYBOARD, 0);
        mInstrumentation.sendKeySync(event);
        mInstrumentation.waitForIdleSync();
    }
    private class MockBaseKeyListener extends BaseKeyListener {
        public int getInputType() {
            return InputType.TYPE_CLASS_DATETIME
                    | InputType.TYPE_DATETIME_VARIATION_DATE;
        }
    }
}
