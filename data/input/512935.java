public class HelloActivityTest extends ActivityInstrumentationTestCase2<HelloActivity> {
    public HelloActivityTest() {
        super(HelloActivity.class);
    }
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }
}
