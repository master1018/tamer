public class FrameLayoutMarginTest extends ActivityInstrumentationTestCase<FrameLayoutMargin> {
    private View mLeftView;
    private View mRightView;
    private View mTopView;
    private View mBottomView;
    private View mParent;
    public FrameLayoutMarginTest() {
        super("com.android.frameworks.coretests", FrameLayoutMargin.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mParent = activity.findViewById(R.id.parent);
        mLeftView = activity.findViewById(R.id.left);
        mRightView = activity.findViewById(R.id.right);
        mTopView = activity.findViewById(R.id.top);
        mBottomView = activity.findViewById(R.id.bottom);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mParent);
        assertNotNull(mLeftView);
        assertNotNull(mRightView);
        assertNotNull(mTopView);
        assertNotNull(mBottomView);
    }
    @MediumTest
    public void testLeftMarginAligned() throws Exception {
        ViewAsserts.assertLeftAligned(mParent, mLeftView,
                ((ViewGroup.MarginLayoutParams) mLeftView.getLayoutParams()).leftMargin);
    }
    @MediumTest
    public void testRightMarginAligned() throws Exception {
        ViewAsserts.assertRightAligned(mParent, mRightView,
                ((ViewGroup.MarginLayoutParams) mRightView.getLayoutParams()).rightMargin);
    }
    @MediumTest
    public void testTopMarginAligned() throws Exception {
        ViewAsserts.assertTopAligned(mParent, mTopView,
                ((ViewGroup.MarginLayoutParams) mTopView.getLayoutParams()).topMargin);
    }
    @MediumTest
    public void testBottomMarginAligned() throws Exception {
        ViewAsserts.assertBottomAligned(mParent, mBottomView,
                ((ViewGroup.MarginLayoutParams) mBottomView.getLayoutParams()).bottomMargin);
    }
}
