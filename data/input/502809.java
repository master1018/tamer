public class FixedWidthTest extends ActivityInstrumentationTestCase<FixedWidth> {
    private View mFixedWidth;
    private View mFixedHeight;
    private View mNonFixedWidth;
    public FixedWidthTest() {
        super("com.android.frameworks.coretests", FixedWidth.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        final FixedWidth activity = getActivity();
        mFixedWidth = activity.findViewById(R.id.fixed_width);
        mNonFixedWidth = activity.findViewById(R.id.non_fixed_width);
        mFixedHeight = activity.findViewById(R.id.fixed_height);
    }
    @MediumTest
    public void testSetUpConditions() throws Exception {
        assertNotNull(mFixedWidth);
        assertNotNull(mFixedHeight);
        assertNotNull(mNonFixedWidth);
    }
    public void testFixedWidth() throws Exception {
        assertEquals(150, mFixedWidth.getWidth());
        assertEquals(mFixedWidth.getWidth(), mNonFixedWidth.getWidth());
    }
    public void testFixedHeight() throws Exception {
        assertEquals(48, mFixedHeight.getHeight());
    }
}
