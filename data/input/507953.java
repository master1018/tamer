public class BaselineAlignmentSpinnerButton extends ActivityInstrumentationTestCase<HorizontalOrientationVerticalAlignment> {
    private View mSpinner;
    private View mButton;
    public BaselineAlignmentSpinnerButton() {
        super("com.android.frameworks.coretests", HorizontalOrientationVerticalAlignment.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final Activity activity = getActivity();
        mSpinner = activity.findViewById(R.id.reminder_value);
        mButton = activity.findViewById(R.id.reminder_remove);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mSpinner);
        assertNotNull(mButton);
    }
    @MediumTest
    public void testChildrenAligned() throws Exception {
        ViewAsserts.assertBaselineAligned(mSpinner, mButton);
    }
}
