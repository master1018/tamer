public class HorizontalFocusSearchTest extends ActivityInstrumentationTestCase<HorizontalFocusSearch> {
    private FocusSearchAlg mFocusFinder;
    private LinearLayout mLayout;
    private Button mLeftTall;
    private Button mMidShort1Top;
    private Button mMidShort2Bottom;
    private Button mRightTall;
    public HorizontalFocusSearchTest() {
        super("com.android.frameworks.coretests", HorizontalFocusSearch.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mFocusFinder = new NewFocusSearchAlg();
        mLayout = getActivity().getLayout();
        mLeftTall = getActivity().getLeftTall();
        mMidShort1Top = getActivity().getMidShort1Top();
        mMidShort2Bottom = getActivity().getMidShort2Bottom();
        mRightTall = getActivity().getRightTall();
    }
    public void testPreconditions() {
        assertNotNull(mLayout);
        assertNotNull(mLeftTall);
        assertNotNull(mMidShort1Top);
        assertNotNull(mMidShort2Bottom);
        assertNotNull(mRightTall);
    }
    public void testSearchFromLeftButton() {
        assertNull("going up from mLeftTall",
                mFocusFinder.findNextFocus(mLayout, mLeftTall, View.FOCUS_UP));
        assertNull("going down from mLeftTall",
                mFocusFinder.findNextFocus(mLayout, mLeftTall, View.FOCUS_DOWN));
        assertNull("going left from mLeftTall",
                mFocusFinder.findNextFocus(mLayout, mLeftTall, View.FOCUS_LEFT));
        assertEquals("going right from mLeftTall",
                mMidShort1Top,
                mFocusFinder.findNextFocus(mLayout, mLeftTall, View.FOCUS_RIGHT));
    }
    public void TODO_testSearchFromMiddleLeftButton() {
        assertNull("going up from mMidShort1Top",
                mFocusFinder.findNextFocus(mLayout, mMidShort1Top, View.FOCUS_UP));
        assertEquals("going down from mMidShort1Top",
                mMidShort2Bottom,
                mFocusFinder.findNextFocus(mLayout, mMidShort1Top, View.FOCUS_DOWN));
        assertEquals("going left from mMidShort1Top",
                mLeftTall,
                mFocusFinder.findNextFocus(mLayout, mMidShort1Top, View.FOCUS_LEFT));
        assertEquals("going right from mMidShort1Top",
                mMidShort2Bottom,
                mFocusFinder.findNextFocus(mLayout, mMidShort1Top, View.FOCUS_RIGHT));
    }
    public void TODO_testSearchFromMiddleRightButton() {
        assertEquals("going up from mMidShort2Bottom",
                mMidShort1Top,
                mFocusFinder.findNextFocus(mLayout, mMidShort2Bottom, View.FOCUS_UP));
        assertNull("going down from mMidShort2Bottom",
                mFocusFinder.findNextFocus(mLayout, mMidShort2Bottom, View.FOCUS_DOWN));
        assertEquals("going left from mMidShort2Bottom",
                mMidShort1Top,
                mFocusFinder.findNextFocus(mLayout, mMidShort2Bottom, View.FOCUS_LEFT));
        assertEquals("goind right from mMidShort2Bottom",
                mRightTall,
                mFocusFinder.findNextFocus(mLayout, mMidShort2Bottom, View.FOCUS_RIGHT));
    }
    public void testSearchFromRightButton() {
        assertNull("going up from mRightTall",
                mFocusFinder.findNextFocus(mLayout, mRightTall, View.FOCUS_UP));
        assertNull("going down from mRightTall",
                mFocusFinder.findNextFocus(mLayout, mRightTall, View.FOCUS_DOWN));
        assertEquals("going left from mRightTall",
                mMidShort2Bottom,
                mFocusFinder.findNextFocus(mLayout, mRightTall, View.FOCUS_LEFT));
        assertNull("going right from mRightTall",
                mFocusFinder.findNextFocus(mLayout, mRightTall, View.FOCUS_RIGHT));
    }
}
