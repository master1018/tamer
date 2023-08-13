public class SetTagsTest extends ActivityInstrumentationTestCase2<Disabled> {
    private Button mView;
    public SetTagsTest() {
        super("com.android.frameworks.coretests", Disabled.class);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mView = (Button) getActivity().findViewById(R.id.disabledButton);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mView);
    }
    @MediumTest
    public void testSetTag() throws Exception {
        mView.setTag("1");
    }
    @MediumTest
    public void testGetTag() throws Exception {
        Object o = new Object();
        mView.setTag(o);
        final Object stored = mView.getTag();
        assertNotNull(stored);
        assertSame("The stored tag is inccorect", o, stored);
    }
    @MediumTest
    public void testSetTagWithKey() throws Exception {
        mView.setTag(R.id.a, "2");
    }
    @MediumTest
    public void testGetTagWithKey() throws Exception {
        Object o = new Object();
        mView.setTag(R.id.a, o);
        final Object stored = mView.getTag(R.id.a);
        assertNotNull(stored);
        assertSame("The stored tag is inccorect", o, stored);
    }
    @MediumTest
    public void testSetTagWithFrameworkId() throws Exception {
        boolean result = false;
        try {
            mView.setTag(android.R.id.list, "2");
        } catch (IllegalArgumentException e) {
            result = true;
        }
        assertTrue("Setting a tag with a framework id did not throw an exception", result);
    }
    @MediumTest
    public void testSetTagWithNoPackageId() throws Exception {
        boolean result = false;
        try {
            mView.setTag(0x000000AA, "2");
        } catch (IllegalArgumentException e) {
            result = true;
        }
        assertTrue("Setting a tag with an id with no package did not throw an exception", result);
    }
    @MediumTest
    public void testSetTagInternalWithFrameworkId() throws Exception {
        mView.setTagInternal(android.R.id.list, "2");
    }
    @MediumTest
    public void testSetTagInternalWithApplicationId() throws Exception {
        boolean result = false;
        try {
            mView.setTagInternal(R.id.a, "2");
        } catch (IllegalArgumentException e) {
            result = true;
        }
        assertTrue("Setting a tag with an id with app package did not throw an exception", result);
    }
}
