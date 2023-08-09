public class IncludeTest extends ActivityInstrumentationTestCase<Include> {
    public IncludeTest() {
        super("com.android.frameworks.coretests", Include.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }
    @MediumTest
    public void testIncluded() throws Exception {
        final Include activity = getActivity();
        final View button1 = activity.findViewById(R.id.included_button);
        assertNotNull("The layout include_button was not included", button1);
        final View button2 = activity.findViewById(R.id.included_button_overriden);
        assertNotNull("The layout include_button was not included with overriden id", button2);
    }
    @MediumTest
    public void testIncludedWithLayoutParams() throws Exception {
        final Include activity = getActivity();
        final View button1 = activity.findViewById(R.id.included_button);
        final View button2 = activity.findViewById(R.id.included_button_overriden);
        assertTrue("Both buttons should have different width",
                button1.getLayoutParams().width != button2.getLayoutParams().width);
        assertTrue("Both buttons should have different height",
                button1.getLayoutParams().height != button2.getLayoutParams().height);
    }
    @MediumTest
    public void testIncludedWithVisibility() throws Exception {
        final Include activity = getActivity();
        final View button1 = activity.findViewById(R.id.included_button_visibility);
        assertEquals("Included button should be invisible", View.INVISIBLE, button1.getVisibility());
    }
    public void testIncludedWithSize() throws Exception {
        final Include activity = getActivity();
        final View button1 = activity.findViewById(R.id.included_button_with_size);
        final ViewGroup.LayoutParams lp = button1.getLayoutParams();
        assertEquals("Included button should be 23dip x 23dip", 23, lp.width);
        assertEquals("Included button should be 23dip x 23dip", 23, lp.height);
    }
}
