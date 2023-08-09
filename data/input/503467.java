public class VerticalGravityTest extends ActivityInstrumentationTestCase<VerticalGravity> {
    private View mReference1;
    private View mReference2;
    private View mReference3;
    private View mTop;
    private View mCenter;
    private View mBottom;
    public VerticalGravityTest() {
        super("com.android.frameworks.coretests", VerticalGravity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final VerticalGravity activity = getActivity();
        mReference1 = activity.findViewById(R.id.reference1);
        mReference2 = activity.findViewById(R.id.reference2);
        mReference3 = activity.findViewById(R.id.reference3);
        mTop        = activity.findViewById(R.id.cell_top);
        mCenter     = activity.findViewById(R.id.cell_center);
        mBottom     = activity.findViewById(R.id.cell_bottom);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mReference1);
        assertNotNull(mReference2);
        assertNotNull(mReference3);
        assertNotNull(mTop);
        assertNotNull(mCenter);
        assertNotNull(mBottom);
    }
    @MediumTest
    public void testTopGravity() throws Exception {
        ViewAsserts.assertTopAligned(mReference1, mTop);
    }
    @MediumTest
    public void testCenterGravity() throws Exception {
        ViewAsserts.assertVerticalCenterAligned(mReference2, mCenter);
    }
    @Suppress
    @MediumTest
    public void testBottomGravity() throws Exception {
        ViewAsserts.assertBottomAligned(mReference3, mBottom);
    }
}
