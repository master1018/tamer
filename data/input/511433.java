@TestTargetClass(LinkMovementMethod.class)
public class LinkMovementMethodTest extends
        ActivityInstrumentationTestCase2<StubActivity> {
    private static final String CONTENT = "clickable\nunclickable\nclickable";
    private LinkMovementMethod mMethod;
    private TextView mView;
    private Spannable mSpannable;
    private MockClickableSpan mClickable0;
    private MockClickableSpan mClickable1;
    public LinkMovementMethodTest() {
        super("com.android.cts.stub", StubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMethod = new LinkMovementMethod();
        mView = new TextView(getActivity());
        mView.setText(CONTENT, BufferType.SPANNABLE);
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                getActivity().setContentView(mView);
            }
        });
        getInstrumentation().waitForIdleSync();
        mSpannable = (Spannable) mView.getText();
        mClickable0 = markClickable(0, CONTENT.indexOf('\n'));
        mClickable1 = markClickable(CONTENT.lastIndexOf('\n'), CONTENT.length());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test constructor LinkMovementMethod#LinkMovementMethod().",
        method = "LinkMovementMethod",
        args = {}
    )
    public void testConstructor() {
        new LinkMovementMethod();
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#getInstance()}. "
                + "This is a method for creating singleton.",
        method = "getInstance",
        args = {}
    )
    public void testGetInstance() {
        MovementMethod method0 = LinkMovementMethod.getInstance();
        assertTrue(method0 instanceof LinkMovementMethod);
        MovementMethod method1 = LinkMovementMethod.getInstance();
        assertNotNull(method1);
        assertSame(method0, method1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#onTakeFocus(TextView, Spannable, int)}. "
                + "The parameter textView is useless.",
        method = "onTakeFocus",
        args = {TextView.class, Spannable.class, int.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#onTakeFocus(TextView, Spannable, "
            + "int) when the params text is null")
    public void testOnTakeFocus() {
        LinkMovementMethod method = new LinkMovementMethod();
        Spannable spannable = new SpannableString("test sequence");
        Selection.setSelection(spannable, 0, spannable.length());
        assertSelection(spannable, 0, spannable.length());
        assertEquals(2, spannable.getSpans(0, spannable.length(), Object.class).length);
        method.onTakeFocus(null, spannable, View.FOCUS_UP);
        assertSelection(spannable, -1);
        assertEquals(1, spannable.getSpans(0, spannable.length(), Object.class).length);
        Object span = spannable.getSpans(0, spannable.length(), Object.class)[0];
        assertEquals(0, spannable.getSpanStart(span));
        assertEquals(0, spannable.getSpanEnd(span));
        assertEquals(Spanned.SPAN_POINT_POINT, spannable.getSpanFlags(span));
        Selection.setSelection(spannable, 0, spannable.length());
        assertSelection(spannable, 0, spannable.length());
        assertEquals(3, spannable.getSpans(0, spannable.length(), Object.class).length);
        method.onTakeFocus(null, spannable, View.FOCUS_RIGHT);
        assertSelection(spannable, -1);
        assertEquals(0, spannable.getSpans(0, spannable.length(), Object.class).length);
        method.onTakeFocus(null, spannable, View.FOCUS_UP);
        Selection.setSelection(spannable, 0, spannable.length());
        assertSelection(spannable, 0, spannable.length());
        assertEquals(3, spannable.getSpans(0, spannable.length(), Object.class).length);
        method.onTakeFocus(null, spannable, 0);
        assertSelection(spannable, -1);
        assertEquals(0, spannable.getSpans(0, spannable.length(), Object.class).length);
        try {
            method.onTakeFocus(new TextView(getActivity()), null, View.FOCUS_RIGHT);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#onKeyDown(TextView, Spannable, int, KeyEvent)}.",
        method = "onKeyDown",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#onKeyDown(TextView, Spannable, "
            + "int, KeyEvent) when the params widget, buffer or event is null")
    public void testOnKeyDown() {
        assertSelection(mSpannable, -1);
        mClickable0.reset();
        mClickable1.reset();
        assertFalse(mMethod.onKeyDown(mView, mSpannable, KeyEvent.KEYCODE_ENTER,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));
        assertFalse(mClickable0.hasCalledOnClick());
        assertFalse(mClickable1.hasCalledOnClick());
        Selection.setSelection(mSpannable, mSpannable.getSpanStart(mClickable0),
                mSpannable.getSpanEnd(mClickable0));
        mClickable0.reset();
        mClickable1.reset();
        assertFalse(mMethod.onKeyDown(mView, mSpannable, KeyEvent.KEYCODE_DPAD_CENTER,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER)));
        assertTrue(mClickable0.hasCalledOnClick());
        assertFalse(mClickable1.hasCalledOnClick());
        Selection.setSelection(mSpannable, mSpannable.getSpanEnd(mClickable0),
                mSpannable.getSpanStart(mClickable1));
        mClickable0.reset();
        mClickable1.reset();
        assertFalse(mMethod.onKeyDown(mView, mSpannable, KeyEvent.KEYCODE_ENTER,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)));
        assertFalse(mClickable0.hasCalledOnClick());
        assertFalse(mClickable1.hasCalledOnClick());
        Selection.selectAll(mSpannable);
        mClickable0.reset();
        mClickable1.reset();
        assertFalse(mMethod.onKeyDown(mView, mSpannable, KeyEvent.KEYCODE_DPAD_CENTER,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER)));
        assertFalse(mClickable0.hasCalledOnClick());
        assertFalse(mClickable1.hasCalledOnClick());
        Selection.setSelection(mSpannable, mSpannable.getSpanEnd(mClickable0),
                mSpannable.getSpanEnd(mClickable1));
        mClickable0.reset();
        mClickable1.reset();
        assertFalse(mMethod.onKeyDown(mView, mSpannable, KeyEvent.KEYCODE_DPAD_CENTER,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER)));
        assertFalse(mClickable0.hasCalledOnClick());
        assertTrue(mClickable1.hasCalledOnClick());
        Selection.setSelection(mSpannable, mSpannable.getSpanEnd(mClickable0),
        mSpannable.getSpanEnd(mClickable1));
        long now = SystemClock.uptimeMillis();
        KeyEvent event = new KeyEvent(now, now, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DPAD_CENTER, 1);
        mClickable0.reset();
        mClickable1.reset();
        assertFalse(mMethod.onKeyDown(mView, mSpannable, KeyEvent.KEYCODE_DPAD_CENTER, event));
        assertFalse(mClickable0.hasCalledOnClick());
        assertFalse(mClickable1.hasCalledOnClick());
        try {
            mMethod.onKeyDown(null, mSpannable, KeyEvent.KEYCODE_DPAD_CENTER,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER));
            fail("The method did not throw NullPointerException when param view is null.");
        } catch (NullPointerException e) {
        }
        try {
            mMethod.onKeyDown(mView, null, KeyEvent.KEYCODE_DPAD_CENTER,
                    new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DPAD_CENTER));
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
        try {
            mMethod.onKeyDown(mView, mSpannable, KeyEvent.KEYCODE_DPAD_CENTER, null);
            fail("The method did not throw NullPointerException when param keyEvent is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#onKeyUp(TextView, Spannable, int, KeyEvent)}. "
                + "It always returns false, and all parameters are never read.",
        method = "onKeyUp",
        args = {TextView.class, Spannable.class, int.class, KeyEvent.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of this method.")
    public void testOnKeyUp() {
        LinkMovementMethod method = new LinkMovementMethod();
        assertFalse(method.onKeyUp(null, null, 0, null));
        assertFalse(method.onKeyUp(new TextView(getActivity()), null, 0, null));
        assertFalse(method.onKeyUp(null, new SpannableString("blahblah"), 0, null));
        assertFalse(method.onKeyUp(null, null, KeyEvent.KEYCODE_0,
                new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_0)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#onTouchEvent(TextView, Spannable, MotionEvent)} ",
        method = "onTouchEvent",
        args = {TextView.class, Spannable.class, MotionEvent.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#onTouchEvent(TextView, "
            + "Spannable, MotionEvent) when the params widget, buffer or event is null")
    public void testOnTouchEvent() {
        assertSelection(mSpannable, -1);
        assertTrue(pressOnLine(0));
        assertSelectClickableLeftToRight(mSpannable, mClickable0);
        assertFalse(mClickable0.hasCalledOnClick());
        assertTrue(releaseOnLine(0));
        assertTrue(mClickable0.hasCalledOnClick());
        assertSelectClickableLeftToRight(mSpannable, mClickable0);
        pressOnLine(1);
        assertSelection(mSpannable, -1);
        assertTrue(pressOnLine(2));
        assertSelectClickableLeftToRight(mSpannable, mClickable1);
        assertFalse(mClickable1.hasCalledOnClick());
        assertTrue(releaseOnLine(2));
        assertTrue(mClickable1.hasCalledOnClick());
        assertSelectClickableLeftToRight(mSpannable, mClickable1);
        releaseOnLine(1);
        assertSelection(mSpannable, -1);
        long now = SystemClock.uptimeMillis();
        int y = (mView.getLayout().getLineTop(1) + mView.getLayout().getLineBottom(1)) / 2;
        try {
            mMethod.onTouchEvent(null, mSpannable,
                    MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 5, y, 0));
            fail("The method did not throw NullPointerException when param view is null.");
        } catch (NullPointerException e) {
        }
        try {
            mMethod.onTouchEvent(mView, null,
                    MotionEvent.obtain(now, now, MotionEvent.ACTION_UP, 5, y, 0));
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
        try {
            mMethod.onTouchEvent(mView, mSpannable, null);
            fail("The method did not throw NullPointerException when param keyEvent is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#up(TextView, Spannable)}. It is protected. "
                + "Use extended class to test.",
        method = "up",
        args = {TextView.class, Spannable.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#up(TextView, Spannable) "
            + "when the params widget or buffer or is null")
    public void testUp() {
        final MyLinkMovementMethod method = new MyLinkMovementMethod();
        assertSelection(mSpannable, -1);
        assertTrue(method.up(mView, mSpannable));
        assertSelectClickableRightToLeft(mSpannable, mClickable1);
        assertTrue(method.up(mView, mSpannable));
        assertSelectClickableRightToLeft(mSpannable, mClickable0);
        assertFalse(method.up(mView, mSpannable));
        assertSelectClickableRightToLeft(mSpannable, mClickable0);
        try {
            method.up(null, mSpannable);
            fail("The method did not throw NullPointerException when param view is null.");
        } catch (NullPointerException e) {
        }
        try {
            method.up(mView, null);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#down(TextView, Spannable)}. It is protected. "
                + "Use extended class to test.",
        method = "down",
        args = {TextView.class, Spannable.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#down(TextView, Spannable) "
            + "when the params widget or buffer or is null")
    public void testDown() {
        final MyLinkMovementMethod method = new MyLinkMovementMethod();
        assertSelection(mSpannable, -1);
        assertTrue(method.down(mView, mSpannable));
        assertSelectClickableLeftToRight(mSpannable, mClickable0);
        assertTrue(method.down(mView, mSpannable));
        assertSelectClickableLeftToRight(mSpannable, mClickable1);
        assertFalse(method.down(mView, mSpannable));
        assertSelectClickableLeftToRight(mSpannable, mClickable1);
        try {
            method.down(null, mSpannable);
            fail("The method did not throw NullPointerException when param view is null.");
        } catch (NullPointerException e) {
        }
        try {
            method.down(mView, null);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#left(TextView, Spannable)}. It is protected. "
                + "Use extended class to test.",
        method = "left",
        args = {TextView.class, Spannable.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#left(TextView, Spannable) "
            + "when the params widget or buffer or is null")
    public void testLeft() {
        final MyLinkMovementMethod method = new MyLinkMovementMethod();
        assertSelection(mSpannable, -1);
        assertTrue(method.left(mView, mSpannable));
        assertSelectClickableRightToLeft(mSpannable, mClickable1);
        assertTrue(method.left(mView, mSpannable));
        assertSelectClickableRightToLeft(mSpannable, mClickable0);
        assertFalse(method.left(mView, mSpannable));
        assertSelectClickableRightToLeft(mSpannable, mClickable0);
        try {
            method.left(null, mSpannable);
            fail("The method did not throw NullPointerException when param view is null.");
        } catch (NullPointerException e) {
        }
        try {
            method.left(mView, null);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#right(TextView, Spannable)}. It is protected. "
                + "Use extended class to test.",
        method = "right",
        args = {TextView.class, Spannable.class}
    )
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#right(TextView, Spannable) "
            + "when the params widget or buffer or is null")
    public void testRight() {
        final MyLinkMovementMethod method = new MyLinkMovementMethod();
        assertSelection(mSpannable, -1);
        assertTrue(method.right(mView, mSpannable));
        assertSelectClickableLeftToRight(mSpannable, mClickable0);
        assertTrue(method.right(mView, mSpannable));
        assertSelectClickableLeftToRight(mSpannable, mClickable1);
        assertFalse(method.right(mView, mSpannable));
        assertSelectClickableLeftToRight(mSpannable, mClickable1);
        try {
            method.right(null, mSpannable);
            fail("The method did not throw NullPointerException when param view is null.");
        } catch (NullPointerException e) {
        }
        try {
            method.right(mView, null);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link LinkMovementMethod#up(TextView, Spannable)}.",
            method = "up",
            args = {TextView.class, Spannable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link LinkMovementMethod#down(TextView, Spannable)}.",
            method = "down",
            args = {TextView.class, Spannable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link LinkMovementMethod#right(TextView, Spannable)}.",
            method = "left",
            args = {TextView.class, Spannable.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Test {@link LinkMovementMethod#left(TextView, Spannable)}.",
            method = "right",
            args = {TextView.class, Spannable.class}
        )
    })
    @UiThreadTest
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. There is no "
            + "document about the behaviour of these methods when the spannable text is not "
            + "clickcable.")
    public void testMoveAroundUnclickable() {
        final MyLinkMovementMethod method = new MyLinkMovementMethod();
        mSpannable.removeSpan(mClickable0);
        mSpannable.removeSpan(mClickable1);
        assertSelection(mSpannable, -1);
        assertFalse(method.up(mView, mSpannable));
        assertSelection(mSpannable, -1);
        assertFalse(method.down(mView, mSpannable));
        assertSelection(mSpannable, -1);
        assertFalse(method.left(mView, mSpannable));
        assertSelection(mSpannable, -1);
        assertFalse(method.right(mView, mSpannable));
        assertSelection(mSpannable, -1);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test {@link LinkMovementMethod#initialize(TextView, Spannable)}.",
        method = "initialize",
        args = {TextView.class, Spannable.class}
    )
    @ToBeFixed(bug = "1695243", explanation = "Android API javadocs are incomplete. @throws clause "
            + "should be added into javadoc of LinkMovementMethod#initialize(TextView, Spannable) "
            + "when the params text is null")
    public void testInitialize() {
        LinkMovementMethod method = new LinkMovementMethod();
        Spannable spannable = new SpannableString("test sequence");
        method.onTakeFocus(null, spannable, View.FOCUS_UP);
        Selection.setSelection(spannable, 0, spannable.length());
        assertSelection(spannable, 0, spannable.length());
        assertEquals(3, spannable.getSpans(0, spannable.length(), Object.class).length);
        method.initialize(null, spannable);
        assertSelection(spannable, -1);
        assertEquals(0, spannable.getSpans(0, spannable.length(), Object.class).length);
        try {
            method.initialize(mView, null);
            fail("The method did not throw NullPointerException when param spannable is null.");
        } catch (NullPointerException e) {
        }
    }
    private MockClickableSpan markClickable(final int start, final int end) {
        final MockClickableSpan clickableSpan = new MockClickableSpan();
        getInstrumentation().runOnMainSync(new Runnable() {
            public void run() {
                mSpannable.setSpan(clickableSpan, start, end, Spanned.SPAN_MARK_MARK);
            }
        });
        getInstrumentation().waitForIdleSync();
        return clickableSpan;
    }
    private boolean performMotionOnLine(int line, int action) {
        int x = (mView.getLayout().getLineStart(line) + mView.getLayout().getLineEnd(line)) / 2;
        int y = (mView.getLayout().getLineTop(line) + mView.getLayout().getLineBottom(line)) / 2;
        long now = SystemClock.uptimeMillis();
        return mMethod.onTouchEvent(mView, mSpannable,
                MotionEvent.obtain(now, now, action, x, y, 0));
    }
    private boolean pressOnLine(int line) {
        return performMotionOnLine(line, MotionEvent.ACTION_DOWN);
    }
    private boolean releaseOnLine(int line) {
        return performMotionOnLine(line, MotionEvent.ACTION_UP);
    }
    private void assertSelection(Spannable spannable, int start, int end) {
        assertEquals(start, Selection.getSelectionStart(spannable));
        assertEquals(end, Selection.getSelectionEnd(spannable));
    }
    private void assertSelection(Spannable spannable, int position) {
        assertSelection(spannable, position, position);
    }
    private void assertSelectClickableLeftToRight(Spannable spannable,
            MockClickableSpan clickableSpan) {
        assertSelection(spannable, spannable.getSpanStart(clickableSpan),
                spannable.getSpanEnd(clickableSpan));
    }
    private void assertSelectClickableRightToLeft(Spannable spannable,
            MockClickableSpan clickableSpan) {
        assertSelection(spannable,  spannable.getSpanEnd(clickableSpan),
                spannable.getSpanStart(clickableSpan));
    }
    private static class MyLinkMovementMethod extends LinkMovementMethod {
        @Override
        protected boolean down(TextView widget, Spannable buffer) {
            return super.down(widget, buffer);
        }
        @Override
        protected boolean left(TextView widget, Spannable buffer) {
            return super.left(widget, buffer);
        }
        @Override
        protected boolean right(TextView widget, Spannable buffer) {
            return super.right(widget, buffer);
        }
        @Override
        protected boolean up(TextView widget, Spannable buffer) {
            return super.up(widget, buffer);
        }
    }
    private static class MockClickableSpan extends ClickableSpan {
        private boolean mHasCalledOnClick;
        @Override
        public void onClick(View widget) {
            mHasCalledOnClick = true;
        }
        public boolean hasCalledOnClick() {
            return mHasCalledOnClick;
        }
        public void reset() {
            mHasCalledOnClick = false;
        }
    }
}
