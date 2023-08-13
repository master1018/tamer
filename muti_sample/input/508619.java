@TestTargetClass(DigitsKeyListener.class)
public class DigitsKeyListenerTest extends
        ActivityInstrumentationTestCase2<KeyListenerStubActivity> {
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private TextView mTextView;
    public DigitsKeyListenerTest(){
        super("com.android.cts.stub", KeyListenerStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mTextView = (TextView) mActivity.findViewById(R.id.keylistener_textview);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DigitsKeyListener",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "DigitsKeyListener",
            args = {boolean.class, boolean.class}
        )
    })
    public void testConstructor() {
        new DigitsKeyListener();
        new DigitsKeyListener(true, true);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "filter",
        args = {CharSequence.class, int.class, int.class, Spanned.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testFilter1() {
        String source = "123456";
        String destString = "dest string";
        DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance();
        SpannableString dest = new SpannableString(destString);
        assertNull(digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
        source = "a1b2c3d";
        assertEquals("123", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        source = "-a1.b2c3d";
        assertEquals("123", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        Object what = new Object();
        Spannable spannableSource = new SpannableString(source);
        spannableSource.setSpan(what, 0, spannableSource.length(), Spanned.SPAN_POINT_POINT);
        Spanned filtered = (Spanned) digitsKeyListener.filter(spannableSource,
                0, spannableSource.length(), dest, 0, dest.length());
        assertEquals("123", filtered.toString());
        assertEquals(Spanned.SPAN_POINT_POINT, filtered.getSpanFlags(what));
        assertEquals(0, filtered.getSpanStart(what));
        assertEquals("123".length(), filtered.getSpanEnd(what));
        assertNull(digitsKeyListener.filter("", 0, 0, dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "filter",
        args = {CharSequence.class, int.class, int.class, Spanned.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testFilter2() {
        String source = "-123456";
        String destString = "dest string without sign and decimal";
        DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance(true, false);
        SpannableString dest = new SpannableString(destString);
        assertNull(digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
        source = "-a1.b2c3d";
        assertEquals("-123", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        source = "-a1-b2c3d";
        assertEquals("-123", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        source = "5-a1-b2c3d";
        assertEquals("5123", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        Object what = new Object();
        Spannable spannableSource = new SpannableString(source);
        spannableSource.setSpan(what, 0, spannableSource.length(), Spanned.SPAN_POINT_POINT);
        Spanned filtered = (Spanned) digitsKeyListener.filter(spannableSource,
                0, spannableSource.length(), dest, 0, dest.length());
        assertEquals("5123", filtered.toString());
        assertEquals(Spanned.SPAN_POINT_POINT, filtered.getSpanFlags(what));
        assertEquals(0, filtered.getSpanStart(what));
        assertEquals("5123".length(), filtered.getSpanEnd(what));
        assertNull(digitsKeyListener.filter("", 0, 0, dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
        source = "-123456";
        String endSign = "789-";
        dest = new SpannableString(endSign);
        assertEquals("", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length() - 1)).toString());
        assertEquals(endSign, dest.toString());
        String startSign = "-789";
        dest = new SpannableString(startSign);
        assertEquals("123456", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 1, dest.length())).toString());
        assertEquals(startSign, dest.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "filter",
        args = {CharSequence.class, int.class, int.class, Spanned.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testFilter3() {
        String source = "123.456";
        String destString = "dest string without sign and decimal";
        DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance(false, true);
        SpannableString dest = new SpannableString(destString);
        assertNull(digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
        source = "-a1.b2c3d";
        assertEquals("1.23", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        source = "a1.b2c3d.";
        assertEquals("123.", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        source = "5.a1.b2c3d";
        assertEquals("51.23", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        Object what = new Object();
        Spannable spannableSource = new SpannableString(source);
        spannableSource.setSpan(what, 0, spannableSource.length(), Spanned.SPAN_POINT_POINT);
        Spanned filtered = (Spanned) digitsKeyListener.filter(spannableSource,
                0, spannableSource.length(), dest, 0, dest.length());
        assertEquals("51.23", filtered.toString());
        assertEquals(Spanned.SPAN_POINT_POINT, filtered.getSpanFlags(what));
        assertEquals(0, filtered.getSpanStart(what));
        assertEquals("51.23".length(), filtered.getSpanEnd(what));
        assertNull(digitsKeyListener.filter("", 0, 0, dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
        source = "123.456";
        String endDecimal = "789.";
        dest = new SpannableString(endDecimal);
        assertEquals("123456", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length() - 1)).toString());
        assertEquals(endDecimal, dest.toString());
        String startDecimal = ".789";
        dest = new SpannableString(startDecimal);
        assertEquals("123456", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 1, dest.length())).toString());
        assertEquals(startDecimal, dest.toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "filter",
        args = {CharSequence.class, int.class, int.class, Spanned.class, int.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete")
    public void testFilter4() {
        String source = "-123.456";
        String destString = "dest string without sign and decimal";
        DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance(true, true);
        SpannableString dest = new SpannableString(destString);
        assertNull(digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
        source = "-a1.b2c3d";
        assertEquals("-1.23", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        source = "a1.b-2c3d.";
        assertEquals("123.", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        source = "-5.a1.b2c3d";
        assertEquals("-51.23", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length())).toString());
        assertEquals(destString, dest.toString());
        Object what = new Object();
        Spannable spannableSource = new SpannableString(source);
        spannableSource.setSpan(what, 0, spannableSource.length(), Spanned.SPAN_POINT_POINT);
        Spanned filtered = (Spanned) digitsKeyListener.filter(spannableSource,
                0, spannableSource.length(), dest, 0, dest.length());
        assertEquals("-51.23", filtered.toString());
        assertEquals(Spanned.SPAN_POINT_POINT, filtered.getSpanFlags(what));
        assertEquals(0, filtered.getSpanStart(what));
        assertEquals("-51.23".length(), filtered.getSpanEnd(what));
        assertNull(digitsKeyListener.filter("", 0, 0, dest, 0, dest.length()));
        assertEquals(destString, dest.toString());
        source = "-123.456";
        String endDecimal = "789.";
        dest = new SpannableString(endDecimal);
        assertEquals("-123456", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length() - 1)).toString());
        assertEquals(endDecimal, dest.toString());
        String startDecimal = ".789";
        dest = new SpannableString(startDecimal);
        assertEquals("123456", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 1, dest.length())).toString());
        assertEquals(startDecimal, dest.toString());
        String endSign = "789-";
        dest = new SpannableString(endSign);
        assertEquals("", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 0, dest.length() - 1)).toString());
        assertEquals(endSign, dest.toString());
        String startSign = "-789";
        dest = new SpannableString(startSign);
        assertEquals("123.456", (digitsKeyListener.filter(source, 0, source.length(),
                dest, 1, dest.length())).toString());
        assertEquals(startSign, dest.toString());
    }
    public void testDigitsKeyListener1() {
        final DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance();
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(digitsKeyListener);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_MINUS);
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("1", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_PERIOD);
        assertEquals("1", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_2);
        assertEquals("12", mTextView.getText().toString());
    }
    public void testDigitsKeyListener2() {
        final DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance(true, false);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(digitsKeyListener);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_MINUS);
        assertEquals("-", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("-1", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_PERIOD);
        assertEquals("-1", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_2);
        assertEquals("-12", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_MINUS);
        assertEquals("-12", mTextView.getText().toString());
    }
    @ToBeFixed(bug = "1728770", explanation = "unexpected IndexOutOfBoundsException occurs" +
            " when set DigitsKeyListener with InputType.TYPE_NUMBER_FLAG_DECIMAL.")
    public void testDigitsKeyListener3() {
    }
    @ToBeFixed(bug = "1728770", explanation = "unexpected IndexOutOfBoundsException occurs" +
            " when set DigitsKeyListener with InputType.TYPE_NUMBER_FLAG_DECIMAL.")
    public void testDigitsKeyListener4() {
    }
    public void testDigitsKeyListener5() {
        final String accepted = "56789";
        final DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance(accepted);
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(digitsKeyListener);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_1);
        assertEquals("", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_5);
        assertEquals("5", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_PERIOD);
        assertEquals("5", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_MINUS);
        assertEquals("5", mTextView.getText().toString());
        mActivity.runOnUiThread(new Runnable() {
            public void run() {
                mTextView.setKeyListener(null);
                mTextView.requestFocus();
            }
        });
        mInstrumentation.waitForIdleSync();
        assertEquals("5", mTextView.getText().toString());
        sendKeys(KeyEvent.KEYCODE_5);
        assertEquals("5", mTextView.getText().toString());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {}
    )
    public void testGetInstance1() {
        DigitsKeyListener listener1 = DigitsKeyListener.getInstance();
        DigitsKeyListener listener2 = DigitsKeyListener.getInstance();
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {boolean.class, boolean.class}
    )
    public void testGetInstance2() {
        DigitsKeyListener listener1 = DigitsKeyListener.getInstance(true, true);
        DigitsKeyListener listener2 = DigitsKeyListener.getInstance(true, true);
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
        listener1 = DigitsKeyListener.getInstance(true, false);
        listener2 = DigitsKeyListener.getInstance(true, false);
        assertNotNull(listener1);
        assertNotNull(listener2);
        assertSame(listener1, listener2);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInstance",
        args = {String.class}
    )
    public void testGetInstance3() {
        DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance("abcdefg");
        assertNotNull(digitsKeyListener);
        digitsKeyListener = DigitsKeyListener.getInstance("Android Test");
        assertNotNull(digitsKeyListener);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getAcceptedChars",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testGetAcceptedChars() {
        MyDigitsKeyListener digitsKeyListener = new MyDigitsKeyListener();
        final char[][] expected = new char[][] {
            new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' },
            new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-' },
            new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.' },
            new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '-', '.' },
        };
        TextMethodUtils.assertEquals(expected[0],
                digitsKeyListener.getAcceptedChars());
        digitsKeyListener = new MyDigitsKeyListener(true, false);
        TextMethodUtils.assertEquals(expected[1],
                digitsKeyListener.getAcceptedChars());
        digitsKeyListener = new MyDigitsKeyListener(false, true);
        TextMethodUtils.assertEquals(expected[2],
                digitsKeyListener.getAcceptedChars());
        digitsKeyListener = new MyDigitsKeyListener(true, true);
        TextMethodUtils.assertEquals(expected[3],
                digitsKeyListener.getAcceptedChars());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "getInputType",
        args = {}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete.")
    public void testGetInputType() {
        DigitsKeyListener digitsKeyListener = DigitsKeyListener.getInstance(false, false);
        int expected = InputType.TYPE_CLASS_NUMBER;
        assertEquals(expected, digitsKeyListener.getInputType());
        digitsKeyListener = DigitsKeyListener.getInstance(true, false);
        expected = InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_SIGNED;
        assertEquals(expected, digitsKeyListener.getInputType());
        digitsKeyListener = DigitsKeyListener.getInstance(false, true);
        expected = InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        assertEquals(expected, digitsKeyListener.getInputType());
        digitsKeyListener = DigitsKeyListener.getInstance(true, true);
        expected = InputType.TYPE_CLASS_NUMBER
                | InputType.TYPE_NUMBER_FLAG_SIGNED
                | InputType.TYPE_NUMBER_FLAG_DECIMAL;
        assertEquals(expected, digitsKeyListener.getInputType());
    }
    private class MyDigitsKeyListener extends DigitsKeyListener {
        public MyDigitsKeyListener() {
            super();
        }
        public MyDigitsKeyListener(boolean sign, boolean decimal) {
            super(sign, decimal);
        }
        @Override
        protected char[] getAcceptedChars() {
            return super.getAcceptedChars();
        }
    }
}
