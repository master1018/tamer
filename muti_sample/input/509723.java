@TestTargetClass(View.class)
public class View_UsingViewsTest extends ActivityInstrumentationTestCase2<UsingViewsStubActivity> {
    private static final String ARGENTINA = "Argentina";
    private static final String AMERICA = "America";
    private static final String CHINA = "China";
    private static final String ARGENTINA_SYMBOL = "football";
    private static final String AMERICA_SYMBOL = "basketball";
    private static final String CHINA_SYMBOL = "table tennis";
    private Activity mActivity;
    private Instrumentation mInstrumentation;
    private EditText mEditText;
    private Button mButtonOk;
    private Button mButtonCancel;
    private TextView mSymbolTextView;
    private TextView mWarningTextView;
    public View_UsingViewsTest() {
        super("com.android.cts.stub", UsingViewsStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
        mInstrumentation = getInstrumentation();
        mEditText = (EditText) mActivity.findViewById(R.id.entry);
        mButtonOk = (Button) mActivity.findViewById(R.id.ok);
        mButtonCancel = (Button) mActivity.findViewById(R.id.cancel);
        mSymbolTextView = (TextView) mActivity.findViewById(R.id.symbolball);
        mWarningTextView = (TextView) mActivity.findViewById(R.id.warning);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setClickable",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isClickable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnClickListener",
            args = {OnClickListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "performClick",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawingCacheEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isDrawingCacheEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawingCacheQuality",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawingCacheQuality",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDrawingCacheBackgroundColor",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawingCacheBackgroundColor",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawingCache",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "buildDrawingCache",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "destroyDrawingCache",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setDuplicateParentStateEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isDuplicateParentStateEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "refreshDrawableState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getDrawableState",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVerticalFadingEdgeEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isVerticalFadingEdgeEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFadingEdgeLength",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHorizontalFadingEdgeEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHorizontalFadingEdgeEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFocusable",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFocusable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFocusableInTouchMode",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isFocusableInTouchMode",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isHorizontalScrollBarEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isVerticalScrollBarEnabled",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setHorizontalScrollBarEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVerticalScrollBarEnabled",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setId",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getId",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "findViewById",
            args = {int.class}
        )
    })
    @UiThreadTest
    public void testSetProperties() {
        mButtonOk.setClickable(true);
        assertTrue(mButtonOk.isClickable());
        MockOnClickOkListener okButtonListener = new MockOnClickOkListener();
        mButtonOk.setOnClickListener(okButtonListener);
        assertFalse(okButtonListener.hasOnClickCalled());
        mButtonOk.performClick();
        assertTrue(okButtonListener.hasOnClickCalled());
        mButtonCancel.setClickable(false);
        assertFalse(mButtonCancel.isClickable());
        MockOnClickCancelListener cancelButtonListener = new MockOnClickCancelListener();
        mButtonCancel.setOnClickListener(cancelButtonListener);
        assertFalse(cancelButtonListener.hasOnClickCalled());
        assertTrue(mButtonCancel.isClickable());
        mButtonCancel.performClick();
        assertTrue(cancelButtonListener.hasOnClickCalled());
        mEditText.setDrawingCacheEnabled(true);
        assertTrue(mEditText.isDrawingCacheEnabled());
        assertEquals(View.DRAWING_CACHE_QUALITY_AUTO, mEditText.getDrawingCacheQuality());
        mEditText.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_LOW);
        assertEquals(View.DRAWING_CACHE_QUALITY_LOW, mEditText.getDrawingCacheQuality());
        mEditText.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        assertEquals(View.DRAWING_CACHE_QUALITY_HIGH, mEditText.getDrawingCacheQuality());
        mEditText.setDrawingCacheBackgroundColor(Color.GREEN);
        assertEquals(Color.GREEN, mEditText.getDrawingCacheBackgroundColor());
        Bitmap b = mEditText.getDrawingCache();
        assertNotNull(b);
        assertEquals(mEditText.getHeight(), b.getHeight());
        assertEquals(mEditText.getWidth(), b.getWidth());
        assertEquals(Color.GREEN, b.getPixel(0, 0));
        mEditText.setDrawingCacheEnabled(false);
        assertFalse(mEditText.isDrawingCacheEnabled());
        mEditText.setDrawingCacheBackgroundColor(Color.YELLOW);
        assertEquals(Color.YELLOW, mEditText.getDrawingCacheBackgroundColor());
        mEditText.buildDrawingCache();
        b = mEditText.getDrawingCache();
        assertNotNull(b);
        assertEquals(mEditText.getHeight(), b.getHeight());
        assertEquals(mEditText.getWidth(), b.getWidth());
        assertEquals(Color.YELLOW, b.getPixel(0, 0));
        mEditText.destroyDrawingCache();
        TextView v = new TextView(mActivity);
        v.setText("Test setDuplicateParentStateEnabled");
        v.setDuplicateParentStateEnabled(false);
        assertFalse(v.isDuplicateParentStateEnabled());
        RelativeLayout parent = (RelativeLayout) mEditText.getParent();
        parent.addView(v);
        assertFalse(parent.getDrawableState().length == v.getDrawableState().length);
        parent.removeView(v);
        v.setDuplicateParentStateEnabled(true);
        assertTrue(v.isDuplicateParentStateEnabled());
        parent.addView(v);
        v.refreshDrawableState();
        assertEquals(parent.getDrawableState().length, v.getDrawableState().length);
        assertEquals(parent.getDrawableState().toString(), v.getDrawableState().toString());
        parent.removeView(v);
        mWarningTextView.setEnabled(false);
        assertFalse(mWarningTextView.isEnabled());
        mWarningTextView.setEnabled(true);
        assertTrue(mWarningTextView.isEnabled());
        mWarningTextView.setVerticalFadingEdgeEnabled(true);
        assertTrue(mWarningTextView.isVerticalFadingEdgeEnabled());
        mWarningTextView.setFadingEdgeLength(10);
        mSymbolTextView.setHorizontalFadingEdgeEnabled(true);
        assertTrue(mSymbolTextView.isHorizontalFadingEdgeEnabled());
        mSymbolTextView.setFadingEdgeLength(100);
        mButtonCancel.setFocusable(false);
        assertFalse(mButtonCancel.isFocusable());
        assertFalse(mButtonCancel.isFocusableInTouchMode());
        mButtonCancel.setFocusable(true);
        assertTrue(mButtonCancel.isFocusable());
        assertFalse(mButtonCancel.isFocusableInTouchMode());
        mButtonCancel.setFocusableInTouchMode(true);
        assertTrue(mButtonCancel.isFocusable());
        assertTrue(mButtonCancel.isFocusableInTouchMode());
        mButtonOk.setFocusable(false);
        assertFalse(mButtonOk.isFocusable());
        assertFalse(mButtonOk.isFocusableInTouchMode());
        mButtonOk.setFocusableInTouchMode(true);
        assertTrue(mButtonOk.isFocusable());
        assertTrue(mButtonOk.isFocusableInTouchMode());
        assertFalse(parent.isHorizontalScrollBarEnabled());
        assertFalse(parent.isVerticalScrollBarEnabled());
        parent.setHorizontalScrollBarEnabled(true);
        assertTrue(parent.isHorizontalScrollBarEnabled());
        parent.setVerticalScrollBarEnabled(true);
        assertTrue(parent.isVerticalScrollBarEnabled());
        assertEquals(View.NO_ID, parent.getId());
        assertEquals(R.id.entry, mEditText.getId());
        assertEquals(R.id.symbolball, mSymbolTextView.getId());
        mSymbolTextView.setId(0x5555);
        assertEquals(0x5555, mSymbolTextView.getId());
        TextView t = (TextView) parent.findViewById(0x5555);
        assertSame(mSymbolTextView, t);
        mSymbolTextView.setId(R.id.symbolball);
        assertEquals(R.id.symbolball, mSymbolTextView.getId());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnFocusChangeListener",
            args = {OnFocusChangeListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisibility",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "hasFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "requestFocus",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setFocusable",
            args = {boolean.class}
        )
    })
    @UiThreadTest
    public void testSetFocus() throws Throwable {
        MockOnFocusChangeListener editListener = new MockOnFocusChangeListener();
        MockOnFocusChangeListener okListener = new MockOnFocusChangeListener();
        MockOnFocusChangeListener cancelListener = new MockOnFocusChangeListener();
        MockOnFocusChangeListener symbolListener = new MockOnFocusChangeListener();
        MockOnFocusChangeListener warningListener = new MockOnFocusChangeListener();
        mEditText.setOnFocusChangeListener(editListener);
        mButtonOk.setOnFocusChangeListener(okListener);
        mButtonCancel.setOnFocusChangeListener(cancelListener);
        mSymbolTextView.setOnFocusChangeListener(symbolListener);
        mWarningTextView.setOnFocusChangeListener(warningListener);
        mSymbolTextView.setText(ARGENTINA_SYMBOL);
        mWarningTextView.setVisibility(View.VISIBLE);
        assertTrue(mEditText.hasFocus());
        assertFalse(mButtonOk.hasFocus());
        assertFalse(mButtonCancel.hasFocus());
        assertFalse(mSymbolTextView.hasFocus());
        assertFalse(mWarningTextView.hasFocus());
        assertFalse(editListener.hasFocus());
        assertFalse(okListener.hasFocus());
        assertFalse(cancelListener.hasFocus());
        assertFalse(symbolListener.hasFocus());
        assertFalse(warningListener.hasFocus());
        assertTrue(mButtonOk.requestFocus());
        assertTrue(mButtonOk.hasFocus());
        assertTrue(okListener.hasFocus());
        assertFalse(mEditText.hasFocus());
        assertFalse(editListener.hasFocus());
        assertTrue(mButtonCancel.requestFocus());
        assertTrue(mButtonCancel.hasFocus());
        assertTrue(cancelListener.hasFocus());
        assertFalse(mButtonOk.hasFocus());
        assertFalse(okListener.hasFocus());
        mSymbolTextView.setFocusable(true);
        assertTrue(mSymbolTextView.requestFocus());
        assertTrue(mSymbolTextView.hasFocus());
        assertTrue(symbolListener.hasFocus());
        assertFalse(mButtonCancel.hasFocus());
        assertFalse(cancelListener.hasFocus());
        mWarningTextView.setFocusable(true);
        assertTrue(mWarningTextView.requestFocus());
        assertTrue(mWarningTextView.hasFocus());
        assertTrue(warningListener.hasFocus());
        assertFalse(mSymbolTextView.hasFocus());
        assertFalse(symbolListener.hasFocus());
        assertTrue(mEditText.requestFocus());
        assertTrue(mEditText.hasFocus());
        assertTrue(editListener.hasFocus());
        assertFalse(mWarningTextView.hasFocus());
        assertFalse(warningListener.hasFocus());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setClickable",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isClickable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setLongClickable",
            args = {boolean.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "isLongClickable",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVisibility",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnClickListener",
            args = {OnClickListener.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setOnLongClickListener",
            args = {OnLongClickListener.class}
        )
    })
    public void testSetupListeners() throws Throwable {
        mButtonOk.setClickable(true);
        assertTrue(mButtonOk.isClickable());
        MockOnClickOkListener okButtonListener = new MockOnClickOkListener();
        mButtonOk.setOnClickListener(okButtonListener);
        mButtonCancel.setClickable(true);
        assertTrue(mButtonCancel.isClickable());
        MockOnClickCancelListener cancelButtonListener = new MockOnClickCancelListener();
        mButtonCancel.setOnClickListener(cancelButtonListener);
        mEditText.setLongClickable(true);
        assertTrue(mEditText.isLongClickable());
        MockOnLongClickListener onLongClickListener = new MockOnLongClickListener();
        mEditText.setOnLongClickListener(onLongClickListener);
        assertFalse(onLongClickListener.isOnLongClickCalled());
        assertNull(onLongClickListener.getView());
        TouchUtils.longClickView(this, mEditText);
        assertTrue(onLongClickListener.isOnLongClickCalled());
        assertSame(mEditText, onLongClickListener.getView());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mEditText.setText("Germany");
            }
        });
        mInstrumentation.waitForIdleSync();
        TouchUtils.clickView(this, mButtonCancel);
        assertEquals("", mEditText.getText().toString());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mEditText.setText(ARGENTINA);
            }
        });
        mInstrumentation.waitForIdleSync();
        TouchUtils.clickView(this, mButtonOk);
        assertEquals(ARGENTINA_SYMBOL, mSymbolTextView.getText().toString());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mEditText.setText(AMERICA);
            }
        });
        mInstrumentation.waitForIdleSync();
        TouchUtils.clickView(this, mButtonOk);
        assertEquals(AMERICA_SYMBOL, mSymbolTextView.getText().toString());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mEditText.setText(CHINA);
            }
        });
        mInstrumentation.waitForIdleSync();
        TouchUtils.clickView(this, mButtonOk);
        assertEquals(CHINA_SYMBOL, mSymbolTextView.getText().toString());
        runTestOnUiThread(new Runnable() {
            public void run() {
                mEditText.setText("Unknown");
            }
        });
        mInstrumentation.waitForIdleSync();
        TouchUtils.clickView(this, mButtonOk);
        assertEquals(View.VISIBLE, mWarningTextView.getVisibility());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setVisibility",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getVisibility",
            args = {}
        )
    })
    @UiThreadTest
    public void testSetVisibility() throws Throwable {
        mActivity.setContentView(R.layout.view_visibility_layout);
        View v1 = mActivity.findViewById(R.id.textview1);
        View v2 = mActivity.findViewById(R.id.textview2);
        View v3 = mActivity.findViewById(R.id.textview3);
        assertNotNull(v1);
        assertNotNull(v2);
        assertNotNull(v3);
        assertEquals(View.VISIBLE, v1.getVisibility());
        assertEquals(View.INVISIBLE, v2.getVisibility());
        assertEquals(View.GONE, v3.getVisibility());
        v1.setVisibility(View.GONE);
        assertEquals(View.GONE, v1.getVisibility());
        v2.setVisibility(View.VISIBLE);
        assertEquals(View.VISIBLE, v2.getVisibility());
        v3.setVisibility(View.INVISIBLE);
        assertEquals(View.INVISIBLE, v3.getVisibility());
    }
    private static class MockOnFocusChangeListener implements OnFocusChangeListener {
        private boolean mHasFocus;
        public void onFocusChange(View v, boolean hasFocus) {
            mHasFocus = hasFocus;
        }
        public boolean hasFocus() {
            return mHasFocus;
        }
    }
    private class MockOnClickOkListener implements OnClickListener {
        private boolean mHasOnClickCalled = false;
        private boolean showPicture(String country) {
            if (ARGENTINA.equals(country)) {
                mSymbolTextView.setText(ARGENTINA_SYMBOL);
                return true;
            } else if (AMERICA.equals(country)) {
                mSymbolTextView.setText(AMERICA_SYMBOL);
                return true;
            } else if (CHINA.equals(country)) {
                mSymbolTextView.setText(CHINA_SYMBOL);
                return true;
            }
            return false;
        }
        public void onClick(View v) {
            mHasOnClickCalled = true;
            String country = mEditText.getText().toString();
            if (!showPicture(country)) {
                mWarningTextView.setVisibility(View.VISIBLE);
            } else if (View.VISIBLE == mWarningTextView.getVisibility()) {
                mWarningTextView.setVisibility(View.INVISIBLE);
            }
        }
        public boolean hasOnClickCalled() {
            return mHasOnClickCalled;
        }
        public void reset() {
            mHasOnClickCalled = false;
        }
    }
    private class MockOnClickCancelListener implements OnClickListener {
        private boolean mHasOnClickCalled = false;
        public void onClick(View v) {
            mHasOnClickCalled = true;
            mEditText.setText(null);
        }
        public boolean hasOnClickCalled() {
            return mHasOnClickCalled;
        }
        public void reset() {
            mHasOnClickCalled = false;
        }
    }
    private static class MockOnLongClickListener implements OnLongClickListener {
        private boolean mIsOnLongClickCalled;
        private View mView;
        public boolean onLongClick(View v) {
            mIsOnLongClickCalled = true;
            mView = v;
            return true;
        }
        public boolean isOnLongClickCalled() {
            return mIsOnLongClickCalled;
        }
        public View getView() {
            return mView;
        }
    }
}
