public class BaselineAlignmentCenterGravityTest extends ActivityInstrumentationTestCase<BaselineAlignmentCenterGravity> {
    private Button mButton1;
    private Button mButton2;
    private Button mButton3;
    public BaselineAlignmentCenterGravityTest() {
        super("com.android.frameworks.coretests", BaselineAlignmentCenterGravity.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mButton1 = (Button) activity.findViewById(R.id.button1);
        mButton2 = (Button) activity.findViewById(R.id.button2);
        mButton3 = (Button) activity.findViewById(R.id.button3);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mButton1);
        assertNotNull(mButton2);
        assertNotNull(mButton3);
    }
    @MediumTest
    public void testChildrenAligned() throws Exception {
        final View parent = (View) mButton1.getParent();
        ViewAsserts.assertTopAligned(mButton1, parent);
        ViewAsserts.assertTopAligned(mButton2, parent);
        ViewAsserts.assertTopAligned(mButton3, parent);
        ViewAsserts.assertBottomAligned(mButton1, parent);
        ViewAsserts.assertBottomAligned(mButton2, parent);
        ViewAsserts.assertBottomAligned(mButton3, parent);
        ViewAsserts.assertTopAligned(mButton1, mButton2);
        ViewAsserts.assertTopAligned(mButton2, mButton3);
        ViewAsserts.assertBottomAligned(mButton1, mButton2);
        ViewAsserts.assertBottomAligned(mButton2, mButton3);
    }
}
