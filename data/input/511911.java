@TestTargetClass(View.class)
public class View_LayoutPositionTest
        extends ActivityInstrumentationTestCase2<ViewLayoutPositionTestStubActivity> {
    private Activity mActivity;
    public View_LayoutPositionTest() {
        super("com.android.cts.stub", ViewLayoutPositionTestStubActivity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getTop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBottom",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getRight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLeft",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "offsetTopAndBottom",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "offsetLeftAndRight",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getBaseLine",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "draw",
            args = {Canvas.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "layout",
            args = {int.class, int.class, int.class, int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMeasuredWidth",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getMeasuredHeight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getLocationOnScreen",
            args = {int[].class}
        )
    })
    @UiThreadTest
    public void testPositionInParent() {
        View parent = mActivity.findViewById(R.id.testparent);
        View view = mActivity.findViewById(R.id.testview);
        int [] pLocation = new int[2];
        int [] vLocation = new int[2];
        Rect pRect = new Rect();
        Rect vRect = new Rect();
        assertEquals(-1, view.getBaseline());
        parent.getLocationOnScreen(pLocation);
        view.getLocationOnScreen(vLocation);
        parent.getDrawingRect(pRect);
        view.getDrawingRect(vRect);
        int left = vLocation[0] - pLocation[0];
        int top = vLocation[1] - pLocation[1];
        int right = left + vRect.width();
        int bottom = top + vRect.height();
        assertEquals(left, view.getLeft());
        assertEquals(top, view.getTop());
        assertEquals(right, view.getRight());
        assertEquals(bottom, view.getBottom());
        assertEquals(vRect.width(), view.getWidth());
        assertEquals(vRect.height(), view.getHeight());
        assertEquals(vRect.width(), view.getMeasuredWidth());
        assertEquals(vRect.height(), view.getMeasuredHeight());
        int v_offset = 10;
        int h_offset = 20;
        view.offsetTopAndBottom(v_offset);
        view.offsetLeftAndRight(h_offset);
        parent.getLocationOnScreen(pLocation);
        view.getLocationOnScreen(vLocation);
        parent.getDrawingRect(pRect);
        view.getDrawingRect(vRect);
        int nleft = vLocation[0] - pLocation[0];
        int ntop = vLocation[1] - pLocation[1];
        int nright = nleft + vRect.width();
        int nbottom = ntop + vRect.height();
        assertEquals(left + h_offset , nleft);
        assertEquals(top + v_offset, ntop);
        assertEquals(right + h_offset, nright);
        assertEquals(bottom + v_offset, nbottom);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingLeft",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingTop",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingRight",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getPaddingBottom",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setPadding",
            args = {int.class, int.class, int.class, int.class}
        )
    })
    public void testPadding() {
        View view = new View(mActivity);
        assertEquals(0, view.getPaddingLeft());
        assertEquals(0, view.getPaddingTop());
        assertEquals(0, view.getPaddingRight());
        assertEquals(0, view.getPaddingBottom());
        view.setPadding(-10, 0, 5, 1000);
        assertEquals(-10, view.getPaddingLeft());
        assertEquals(0, view.getPaddingTop());
        assertEquals(5, view.getPaddingRight());
        assertEquals(1000, view.getPaddingBottom());
    }
}
