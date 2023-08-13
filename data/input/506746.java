public class SkeletonAppTest extends ActivityInstrumentationTestCase2<SkeletonActivity> {
    public SkeletonAppTest() {
        super(SkeletonActivity.class);
    }
    public void testActivityTestCaseSetUpProperly() {
        assertNotNull("activity should be launched successfully", getActivity());
    }
}
