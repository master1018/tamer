@TestTargetClass(QwertyKeyListener.class)
public class QwertyKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public QwertyKeyListenerTest() {
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
        method = "QwertyKeyListener",
        args = {TextKeyListener.Capitalize.class, boolean.class}
    )
    public void testConstructor() {
        new QwertyKeyListener(Capitalize.NONE, false);
        new QwertyKeyListener(Capitalize.WORDS, true);
        new QwertyKeyListener(null, true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1738241", explanation = "can not correct spelling automatically")
    public void testPressKey1() {
        final QwertyKeyListener qwertyKeyListener
                = QwertyKeyListener.getInstance(true, Capitalize.NONE);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(qwertyKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("h", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_E);
        assertEquals("he", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("hel", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("hell", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_U);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey2() {
        final QwertyKeyListener qwertyKeyListener
                = QwertyKeyListener.getInstance(false, Capitalize.NONE);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(qwertyKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("h", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_E);
        assertEquals("he", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("hel", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("hell", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_U);
        assertEquals("hellu", mTextView.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey3() {
        final QwertyKeyListener qwertyKeyListener
                = QwertyKeyListener.getInstance(false, Capitalize.SENTENCES);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(qwertyKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("H", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_E);
        assertEquals("He", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("Hel", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("Hell", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_U);
        assertEquals("Hellu", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_PERIOD);
        assertEquals("Hellu.", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_SPACE);
        assertEquals("Hellu. ", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("Hellu. H", mTextView.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey4() {
        final QwertyKeyListener qwertyKeyListener
                = QwertyKeyListener.getInstance(false, Capitalize.WORDS);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(qwertyKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("H", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_E);
        assertEquals("He", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("Hel", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("Hell", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_U);
        assertEquals("Hellu", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_SPACE);
        assertEquals("Hellu ", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("Hellu H", mTextView.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyDown",
        args = {View.class, Editable.class, int.class, KeyEvent.class}
    )
    public void testPressKey5() {
        final QwertyKeyListener qwertyKeyListener
                = QwertyKeyListener.getInstance(false, Capitalize.CHARACTERS);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(qwertyKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("H", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_E);
        assertEquals("HE", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("HEL", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_L);
        assertEquals("HELL", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_U);
        assertEquals("HELLU", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_SPACE);
        assertEquals("HELLU ", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_H);
        assertEquals("HELLU H", mTextView.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {boolean.class, TextKeyListener.Capitalize.class}
    )
    public void testGetInstance() {
        QwertyKeyListener listener1 = QwertyKeyListener.getInstance(true, Capitalize.WORDS);
        QwertyKeyListener listener2 = QwertyKeyListener.getInstance(true, Capitalize.WORDS);
        QwertyKeyListener listener3 = QwertyKeyListener.getInstance(false, Capitalize.WORDS);
        QwertyKeyListener listener4 = QwertyKeyListener.getInstance(true, Capitalize.SENTENCES);
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
        assertNotSame(listener1, listener3);
        assertNotSame(listener1, listener4);
        assertNotSame(listener4, listener3);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "markAsReplaced",
        args = {Spannable.class, int.class, int.class, String.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add NPE description in javadoc.")
    public void testMarkAsReplaced() {
        SpannableStringBuilder content = new SpannableStringBuilder("123456");
        Object[] repl = content.getSpans(0, content.length(), Object.class);
        assertEquals(0, repl.length);
        QwertyKeyListener.markAsReplaced(content, 0, content.length(), "abcd");
        repl = content.getSpans(0, content.length(), Object.class);
        assertEquals(1, repl.length);
        assertEquals(0, content.getSpanStart(repl[0]));
        assertEquals(content.length(), content.getSpanEnd(repl[0]));
        assertEquals(Spannable.SPAN_EXCLUSIVE_EXCLUSIVE, content.getSpanFlags(repl[0]));
        QwertyKeyListener.markAsReplaced(content, 1, 2, "abcd");
        repl = content.getSpans(0, content.length(), Object.class);
        assertEquals(1, repl.length);
        assertEquals(1, content.getSpanStart(repl[0]));
        assertEquals(2, content.getSpanEnd(repl[0]));
        assertEquals(Spannable.SPAN_EXCLUSIVE_EXCLUSIVE, content.getSpanFlags(repl[0]));
        try {
            QwertyKeyListener.markAsReplaced(null, 1, 2, "abcd");
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        try {
            QwertyKeyListener.markAsReplaced(content, 1, 2, null);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    public void testGetInputType() {
        QwertyKeyListener listener = QwertyKeyListener.getInstance(false, Capitalize.NONE);
        int expected = InputType.TYPE_CLASS_TEXT;
        assertEquals(expected, listener.getInputType());
        listener = QwertyKeyListener.getInstance(false, Capitalize.CHARACTERS);
        expected = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
        assertEquals(expected, listener.getInputType());
    }
}
