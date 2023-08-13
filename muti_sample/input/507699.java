@TestTargetClass(TextKeyListener.class)
public class TextKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private static final long TIME_OUT = 3000;
    private KeyListenerStubActivity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public TextKeyListenerTest() {
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
        method = "TextKeyListener",
        args = {TextKeyListener.Capitalize.class, boolean.class}
    )
    public void testConstructor() {
        new TextKeyListener(Capitalize.NONE, true);
        new TextKeyListener(null, true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "shouldCap",
        args = {TextKeyListener.Capitalize.class, CharSequence.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add NPE description in javadoc.")
    public void testShouldCap() {
        String str = "hello world! man";
        assertFalse(TextKeyListener.shouldCap(Capitalize.NONE, str, 0));
        assertTrue(TextKeyListener.shouldCap(Capitalize.SENTENCES, str, 0));
        assertTrue(TextKeyListener.shouldCap(Capitalize.WORDS, str, 0));
        assertTrue(TextKeyListener.shouldCap(Capitalize.CHARACTERS, str, 0));
        assertFalse(TextKeyListener.shouldCap(Capitalize.NONE, str, 6));
        assertFalse(TextKeyListener.shouldCap(Capitalize.SENTENCES, str, 6));
        assertTrue(TextKeyListener.shouldCap(Capitalize.WORDS, str, 6));
        assertTrue(TextKeyListener.shouldCap(Capitalize.CHARACTERS, str, 6));
        assertFalse(TextKeyListener.shouldCap(Capitalize.NONE, str, 13));
        assertTrue(TextKeyListener.shouldCap(Capitalize.SENTENCES, str, 13));
        assertTrue(TextKeyListener.shouldCap(Capitalize.WORDS, str, 13));
        assertTrue(TextKeyListener.shouldCap(Capitalize.CHARACTERS, str, 13));
        assertFalse(TextKeyListener.shouldCap(Capitalize.NONE, str, 14));
        assertFalse(TextKeyListener.shouldCap(Capitalize.SENTENCES, str, 14));
        assertFalse(TextKeyListener.shouldCap(Capitalize.WORDS, str, 14));
        assertTrue(TextKeyListener.shouldCap(Capitalize.CHARACTERS, str, 14));
        try {
            TextKeyListener.shouldCap(Capitalize.WORDS, null, 16);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "onSpanAdded is a non-operation function.",
        method = "onSpanAdded",
        args = {Spannable.class, Object.class, int.class, int.class}
    )
    public void testOnSpanAdded() {
        final MockTextKeyListener textKeyListener
                = new MockTextKeyListener(Capitalize.CHARACTERS, true);
        final Spannable text = new SpannableStringBuilder("123456");
        assertFalse(textKeyListener.hadAddedSpan());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(textKeyListener);
                mTextView.setText(text, BufferType.EDITABLE);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertTrue(textKeyListener.hadAddedSpan());
        textKeyListener.release();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {boolean.class, TextKeyListener.Capitalize.class}
    )
    public void testGetInstance1() {
        TextKeyListener listener1 = TextKeyListener.getInstance(true, Capitalize.WORDS);
        TextKeyListener listener2 = TextKeyListener.getInstance(true, Capitalize.WORDS);
        TextKeyListener listener3 = TextKeyListener.getInstance(false, Capitalize.WORDS);
        TextKeyListener listener4 = TextKeyListener.getInstance(true, Capitalize.CHARACTERS);
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
        assertNotSame(listener1, listener3);
        assertNotSame(listener1, listener4);
        assertNotSame(listener4, listener3);
        listener1.release();
        listener2.release();
        listener3.release();
        listener4.release();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance2() {
        TextKeyListener listener1 = TextKeyListener.getInstance();
        TextKeyListener listener2 = TextKeyListener.getInstance();
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
        listener1.release();
        listener2.release();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onSpanChanged",
        args = {Spannable.class, Object.class, int.class, int.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete, " +
            "should add @throws clause into javadoc")
    public void testOnSpanChanged() {
        TextKeyListener textKeyListener = TextKeyListener.getInstance();
        final Spannable text = new SpannableStringBuilder("123456");
        textKeyListener.onSpanChanged(text, Selection.SELECTION_END, 0, 0, 0, 0);
        try {
            textKeyListener.onSpanChanged(null, Selection.SELECTION_END, 0, 0, 0, 0);
            fail("should throw NullPointerException.");
        } catch (NullPointerException e) {
        }
        textKeyListener.release();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clear",
        args = {Editable.class}
    )
    @UiThreadTest
    public void testClear() {
        CharSequence text = "123456";
        mTextView.setText(text, BufferType.EDITABLE);
        Editable content = (Editable) mTextView.getText();
        assertEquals(text, content.toString());
        TextKeyListener.clear(content);
        assertEquals("", content.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "onSpanRemoved is a non-operation function.",
        method = "onSpanRemoved",
        args = {Spannable.class, Object.class, int.class, int.class}
    )
    public void testOnSpanRemoved() {
        TextKeyListener textKeyListener = new TextKeyListener(Capitalize.CHARACTERS, true);
        final Spannable text = new SpannableStringBuilder("123456");
        textKeyListener.onSpanRemoved(text, new Object(), 0, 0);
        textKeyListener.release();
    }
    private void waitForListenerTimeout() {
        try {
            Thread.sleep(TIME_OUT);
        } catch (InterruptedException e) {
            fail("thrown unexpected InterruptedException when sleep.");
        }
    }
    private int getKeyboardType() {
        KeyCharacterMap kmap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
        return kmap.getKeyboardType();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyDown",
            args = {View.class, Editable.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "onKeyUp",
            args = {View.class, Editable.class, int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "release",
            args = {}
        )
    })
    public void testPressKey() {
        final TextKeyListener textKeyListener
                = TextKeyListener.getInstance(false, Capitalize.NONE);
        int keyType = getKeyboardType();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(textKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_4);
        waitForListenerTimeout();
        String text = mTextView.getText().toString();
        if (KeyCharacterMap.ALPHA == keyType) {
            assertEquals("4", text);
        } else if (KeyCharacterMap.NUMERIC == keyType) {
            assertEquals("g", text);
        } else {
            assertEquals("", text);
        }
        textKeyListener.release();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onKeyOther",
        args = {View.class, Editable.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1731439", explanation = "onKeyOther doesn't insert the" +
            " event's text into content.")
    public void testOnKeyOther() {
        final String text = "abcd";
        final TextKeyListener textKeyListener
                = TextKeyListener.getInstance(false, Capitalize.NONE);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setText("", BufferType.EDITABLE);
                mTextView.requestFocus();
                Selection.setSelection((Editable) mTextView.getText(), 0, 0);
                mTextView.setKeyListener(textKeyListener);
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        KeyEvent event = new KeyEvent(SystemClock.uptimeMillis(), text,
                1, KeyEvent.FLAG_WOKE_HERE);
        mInstrumentation.sendKeySync(event);
        mInstrumentation.waitForIdleSync();
        textKeyListener.release();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    public void testGetInputType() {
        TextKeyListener listener = TextKeyListener.getInstance(false, Capitalize.NONE);
        int expected = InputType.TYPE_CLASS_TEXT;
        assertEquals(expected, listener.getInputType());
        listener = TextKeyListener.getInstance(false, Capitalize.CHARACTERS);
        expected = InputType.TYPE_CLASS_TEXT
                | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;
        assertEquals(expected, listener.getInputType());
        listener.release();
    }
    private class MockTextKeyListener extends TextKeyListener {
        private boolean mHadAddedSpan;
        public MockTextKeyListener(Capitalize cap, boolean autotext) {
            super(cap, autotext);
        }
        @Override
        public void onSpanAdded(Spannable s, Object what, int start, int end) {
            mHadAddedSpan = true;
            super.onSpanAdded(s, what, start, end);
        }
        public boolean hadAddedSpan() {
            return mHadAddedSpan;
        }
    }
}
