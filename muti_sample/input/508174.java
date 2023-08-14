public class BaselineAlignmentZeroWidthAndWeightTest extends ActivityInstrumentationTestCase<BaselineAlignmentZeroWidthAndWeight> {
    private Button mShowButton;
    public BaselineAlignmentZeroWidthAndWeightTest() {
        super("com.android.frameworks.coretests", BaselineAlignmentZeroWidthAndWeight.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mShowButton = (Button) activity.findViewById(R.id.show);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mShowButton);        
    }
    @MediumTest
    public void testComputeTexViewWithoutIllegalArgumentException() throws Exception {
        assertTrue(mShowButton.hasFocus());
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        getInstrumentation().waitForIdleSync();
        final ExceptionTextView etv = (ExceptionTextView) getActivity()
                .findViewById(R.id.routeToField);
        assertFalse("exception test view should not fail", etv.isFailed());
    }
}
