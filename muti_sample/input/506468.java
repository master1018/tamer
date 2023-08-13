public class MergeTest extends ActivityInstrumentationTestCase<Merge> {
    public MergeTest() {
        super("com.android.frameworks.coretests", Merge.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    @MediumTest
    public void testMerged() throws Exception {
        final Merge activity = getActivity();
        final ViewGroup layout = activity.getLayout();
        assertEquals("The layout wasn't merged", 7, layout.getChildCount());
    }
}
