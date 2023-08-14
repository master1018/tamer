@TestTargetClass(DialerFilter.class)
public class DialerFilterTest extends ActivityInstrumentationTestCase2<DialerFilterStubActivity> {
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private DialerFilter mDialerFilter;
    public DialerFilterTest() {
        super("com.android.cts.stub", DialerFilterStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mDialerFilter = (DialerFilter) mActivity.findViewById(R.id.dialer_filter);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DialerFilter",
            args = {Context.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DialerFilter",
            args = {Context.class, AttributeSet.class}
        )
    })
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testConstructor() {
        final XmlPullParser parser = mActivity.getResources().getXml(R.layout.dialerfilter_layout);
        final AttributeSet attrs = Xml.asAttributeSet(parser);
        new DialerFilter(mActivity);
        new DialerFilter(mActivity, attrs);
    }
    @TestTargetNew(
        level = TestLevel.SUFFICIENT,
        method = "isQwertyKeyboard",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testIsQwertyKeyboard() {
        mDialerFilter.isQwertyKeyboard();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "onKeyUp",
            args = {int.class, KeyEvent.class}
        ),
        @TestTargetNew(
            level = TestLevel.SUFFICIENT,
            method = "onKeyDown",
            args = {int.class, KeyEvent.class}
        )
    })
    public void testOnKeyUpDown() {
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                mDialerFilter.setMode(DialerFilter.DIGITS_ONLY);
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3);
        assertEquals("", mDialerFilter.getLetters().toString());
        assertEquals("123", mDialerFilter.getDigits().toString());
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                mDialerFilter.clearText();
                mDialerFilter.setMode(DialerFilter.LETTERS_ONLY);
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_G);
        assertEquals("ADG", mDialerFilter.getLetters().toString());
        assertEquals("", mDialerFilter.getDigits().toString());
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                mDialerFilter.clearText();
                mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_G);
        assertEquals("ADG", mDialerFilter.getLetters().toString());
        mInstrumentation.runOnMainSync(new Runnable() {
            public void run() {
                mDialerFilter.clearText();
                mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
            }
        });
        mInstrumentation.waitForIdleSync();
        sendKeys(KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_3);
        assertEquals("123", mDialerFilter.getDigits().toString());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMode",
            args = {}
        )
    })
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testAccessMode() {
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS_NO_LETTERS);
        assertEquals(DialerFilter.DIGITS_AND_LETTERS_NO_LETTERS, mDialerFilter.getMode());
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
        assertEquals(DialerFilter.DIGITS_AND_LETTERS, mDialerFilter.getMode());
        mDialerFilter.setMode(-1);
        assertEquals(-1, mDialerFilter.getMode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getLetters",
        args = {}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testGetLetters() {
        assertEquals("", mDialerFilter.getLetters().toString());
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
        mDialerFilter.append("ANDROID");
        assertEquals("ANDROID", mDialerFilter.getLetters().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getDigits",
        args = {}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testGetDigits() {
        assertEquals("", mDialerFilter.getDigits().toString());
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
        mDialerFilter.append("12345");
        assertEquals("12345", mDialerFilter.getDigits().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getFilterText",
        args = {}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testGetFilterText() {
        assertEquals("", mDialerFilter.getFilterText().toString());
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
        mDialerFilter.append("CTS12345");
        assertEquals("CTS12345", mDialerFilter.getFilterText().toString());
        mDialerFilter.setMode(DialerFilter.DIGITS_ONLY);
        assertEquals("12345", mDialerFilter.getFilterText().toString());
        mDialerFilter.setMode(DialerFilter.LETTERS_ONLY);
        assertEquals("CTS12345", mDialerFilter.getFilterText().toString());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "append",
            args = {java.lang.String.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setMode",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLetters",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDigits",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getFilterText",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "clearText",
            args = {}
        )
    })
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testAppend() {
        mDialerFilter.setMode(DialerFilter.LETTERS_ONLY);
        mDialerFilter.append("ANDROID");
        assertEquals("ANDROID", mDialerFilter.getLetters().toString());
        assertEquals("", mDialerFilter.getDigits().toString());
        assertEquals("ANDROID", mDialerFilter.getFilterText().toString());
        mDialerFilter.setMode(DialerFilter.DIGITS_ONLY);
        mDialerFilter.append("123");
        assertEquals("", mDialerFilter.getLetters().toString());
        assertEquals("123", mDialerFilter.getDigits().toString());
        assertEquals("123", mDialerFilter.getFilterText().toString());
        mDialerFilter.clearText();
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
        mDialerFilter.append("ABC123DEF456GHI789");
        assertEquals("ABC123DEF456GHI789", mDialerFilter.getLetters().toString());
        assertEquals("123456789", mDialerFilter.getDigits().toString());
        assertEquals("ABC123DEF456GHI789", mDialerFilter.getFilterText().toString());
        mDialerFilter.clearText();
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS_NO_DIGITS);
        mDialerFilter.append("ABC123DEF456GHI789");
        assertEquals("ABC123DEF456GHI789", mDialerFilter.getLetters().toString());
        assertEquals("", mDialerFilter.getDigits().toString());
        assertEquals("ABC123DEF456GHI789", mDialerFilter.getFilterText().toString());
        mDialerFilter.clearText();
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS_NO_LETTERS);
        mDialerFilter.append("ABC123DEF456GHI789");
        assertEquals("", mDialerFilter.getLetters().toString());
        assertEquals("123456789", mDialerFilter.getDigits().toString());
        assertEquals("", mDialerFilter.getFilterText().toString());
        mDialerFilter.clearText();
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
        mDialerFilter.append("");
        assertEquals("", mDialerFilter.getLetters().toString());
        assertEquals("", mDialerFilter.getDigits().toString());
        assertEquals("", mDialerFilter.getFilterText().toString());
        try {
            mDialerFilter.append(null);
            fail("A NullPointerException should be thrown out.");
        } catch (final NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "clearText",
        args = {}
    )
    @UiThreadTest
    public void testClearText() {
        assertEquals("", mDialerFilter.getLetters().toString());
        assertEquals("", mDialerFilter.getDigits().toString());
        mDialerFilter.setMode(DialerFilter.DIGITS_AND_LETTERS);
        mDialerFilter.append("CTS12345");
        assertEquals("CTS12345", mDialerFilter.getLetters().toString());
        assertEquals("12345", mDialerFilter.getDigits().toString());
        mDialerFilter.clearText();
        assertEquals("", mDialerFilter.getLetters().toString());
        assertEquals("", mDialerFilter.getDigits().toString());
        assertEquals(DialerFilter.DIGITS_AND_LETTERS, mDialerFilter.getMode());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setLettersWatcher",
        args = {TextWatcher.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testSetLettersWatcher() {
        MockTextWatcher tw = new MockTextWatcher("A");
        Spannable span = (Spannable) mDialerFilter.getLetters();
        assertEquals(-1, span.getSpanStart(tw));
        assertEquals(-1, span.getSpanEnd(tw));
        mDialerFilter.setMode(DialerFilter.LETTERS_ONLY);
        mDialerFilter.setLettersWatcher(tw);
        mDialerFilter.append("ANDROID");
        assertEquals("ANDROID", tw.getText());
        span = (Spannable) mDialerFilter.getLetters();
        assertEquals(0, span.getSpanStart(tw));
        assertEquals(mDialerFilter.getLetters().length(), span.getSpanEnd(tw));
        assertEquals("ANDROID", span.toString());
        tw = new MockTextWatcher("");
        mDialerFilter.setLettersWatcher(tw);
        mDialerFilter.append("");
        assertEquals("", tw.getText());
        try {
            mDialerFilter.setLettersWatcher(new MockTextWatcher(null));
            mDialerFilter.append(null);
            fail("A NullPointerException should be thrown out.");
        } catch (final NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setDigitsWatcher",
        args = {TextWatcher.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testSetDigitsWatcher() {
        final MockTextWatcher tw = new MockTextWatcher("9");
        Spannable span = (Spannable) mDialerFilter.getDigits();
        assertEquals(-1, span.getSpanStart(tw));
        assertEquals(-1, span.getSpanEnd(tw));
        mDialerFilter.setDigitsWatcher(tw);
        assertEquals(0, span.getSpanStart(tw));
        assertEquals(mDialerFilter.getDigits().length(), span.getSpanEnd(tw));
        mDialerFilter.setMode(DialerFilter.DIGITS_ONLY);
        mDialerFilter.append("12345");
        assertEquals("12345", tw.getText());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "setFilterWatcher",
        args = {TextWatcher.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testSetFilterWatcher() {
        final MockTextWatcher tw = new MockTextWatcher("A");
        Spannable span = (Spannable) mDialerFilter.getLetters();
        assertEquals(-1, span.getSpanStart(tw));
        assertEquals(-1, span.getSpanEnd(tw));
        mDialerFilter.setMode(DialerFilter.LETTERS_ONLY);
        mDialerFilter.setFilterWatcher(tw);
        mDialerFilter.append("ANDROID");
        assertEquals("ANDROID", tw.getText());
        span = (Spannable) mDialerFilter.getLetters();
        assertEquals(0, span.getSpanStart(tw));
        assertEquals(mDialerFilter.getLetters().length(), span.getSpanEnd(tw));
        mDialerFilter.setMode(DialerFilter.DIGITS_ONLY);
        mDialerFilter.setFilterWatcher(tw);
        mDialerFilter.append("12345");
        assertEquals("12345", tw.getText());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "removeFilterWatcher",
        args = {TextWatcher.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testRemoveFilterWatcher() {
        final MockTextWatcher tw = new MockTextWatcher("A");
        Spannable span = (Spannable) mDialerFilter.getLetters();
        assertEquals(-1, span.getSpanStart(tw));
        assertEquals(-1, span.getSpanEnd(tw));
        mDialerFilter.setMode(DialerFilter.LETTERS_ONLY);
        mDialerFilter.setFilterWatcher(tw);
        mDialerFilter.append("ANDROID");
        assertEquals("ANDROID", tw.getText());
        span = (Spannable) mDialerFilter.getLetters();
        assertEquals(0, span.getSpanStart(tw));
        assertEquals(mDialerFilter.getLetters().length(), span.getSpanEnd(tw));
        mDialerFilter.removeFilterWatcher(tw);
        mDialerFilter.append("GOLF");
        assertEquals("ANDROID", tw.getText());
        assertEquals(-1, span.getSpanStart(tw));
        assertEquals(-1, span.getSpanEnd(tw));
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onFinishInflate",
        args = {}
    )
    public void testOnFinishInflate() {
    }
    @TestTargetNew(
        level = TestLevel.NOT_NECESSARY,
        method = "onFocusChanged",
        args = {boolean.class, int.class, android.graphics.Rect.class}
    )
    public void testOnFocusChanged() {
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "onModeChange",
        args = {int.class, int.class}
    )
    @UiThreadTest
    public void testOnModechange() {
        final MockDialerFilter dialerFilter = createMyDialerFilter();
        dialerFilter.onFinishInflate();
        assertEquals(0, dialerFilter.getOldMode());
        assertEquals(MockDialerFilter.DIGITS_AND_LETTERS, dialerFilter.getNewMode());
        dialerFilter.setMode(MockDialerFilter.DIGITS_AND_LETTERS_NO_LETTERS);
        assertEquals(MockDialerFilter.DIGITS_AND_LETTERS, dialerFilter.getOldMode());
        assertEquals(MockDialerFilter.DIGITS_AND_LETTERS_NO_LETTERS, dialerFilter.getNewMode());
        dialerFilter.setMode(MockDialerFilter.DIGITS_AND_LETTERS_NO_DIGITS);
        assertEquals(MockDialerFilter.DIGITS_AND_LETTERS_NO_LETTERS, dialerFilter.getOldMode());
        assertEquals(MockDialerFilter.DIGITS_AND_LETTERS_NO_DIGITS, dialerFilter.getNewMode());
    }
    private MockDialerFilter createMyDialerFilter() {
        final MockDialerFilter dialerFilter = new MockDialerFilter(mActivity);
        final EditText text1 = new EditText(mActivity);
        text1.setId(com.android.internal.R.id.hint);
        final EditText text2 = new EditText(mActivity);
        text2.setId(com.android.internal.R.id.primary);
        dialerFilter.addView(text1, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        dialerFilter.addView(text2, new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));
        return dialerFilter;
    }
    private class MockTextWatcher implements TextWatcher {
        private String mString;
        public MockTextWatcher(final String s) {
            mString = s;
        }
        public void beforeTextChanged(final CharSequence s, final int start, final int count,
                final int after) {
            Log.d("DialerFilterTest", "MockTextWatcher beforeTextChanged");
        }
        public void onTextChanged(final CharSequence s, final int start, final int before,
                final int count) {
            Log.d("DialerFilterTest", "MockTextWatcher onTextChanged");
            mString = s.toString();
        }
        public void afterTextChanged(final Editable s) {
            Log.d("DialerFilterTest", "MockTextWatcher afterTextChanged");
        }
        public String getText() {
            return mString;
        }
    }
    private class MockDialerFilter extends DialerFilter {
        private int mOldMode = 0;
        private int mNewMode = 0;
        public MockDialerFilter(Context context) {
            super(context);
        }
        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
        }
        @Override
        protected void onModeChange(final int oldMode, final int newMode) {
            super.onModeChange(oldMode, newMode);
            mOldMode = oldMode;
            mNewMode = newMode;
        }
        public int getOldMode() {
            return mOldMode;
        }
        public int getNewMode() {
            return mNewMode;
        }
    }
}
