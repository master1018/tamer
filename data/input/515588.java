public class HorizontalGravityTest extends ActivityInstrumentationTestCase<HorizontalGravity> {
    private View mReference;
    private View mCenter;
    private View mBottomRight;
    private View mLeft;
    public HorizontalGravityTest() {
        super("com.android.frameworks.coretests", HorizontalGravity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final HorizontalGravity activity = getActivity();
        mReference   = activity.findViewById(R.id.reference);
        mCenter      = activity.findViewById(R.id.center);
        mBottomRight = activity.findViewById(R.id.bottomRight);
        mLeft        = activity.findViewById(R.id.left);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mReference);
        assertNotNull(mCenter);
        assertNotNull(mBottomRight);
        assertNotNull(mLeft);
    }
    @MediumTest
    public void testCenterGravity() throws Exception {
        ViewAsserts.assertHorizontalCenterAligned(mReference, mCenter);
    }
    @MediumTest
    public void testLeftGravity() throws Exception {
        ViewAsserts.assertLeftAligned(mReference, mLeft);
    }
    @MediumTest
    public void testRightGravity() throws Exception {
        ViewAsserts.assertRightAligned(mReference, mBottomRight);
    }
}
