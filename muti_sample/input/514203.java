public class DescendantFocusabilityTest extends ActivityInstrumentationTestCase<DescendantFocusability> {
    private DescendantFocusability a;
    public DescendantFocusabilityTest() {
        super("com.android.frameworks.coretests", DescendantFocusability.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        a = getActivity();
    }
    @MediumTest
    public void testPreconditions() {
        assertEquals(ViewGroup.FOCUS_BEFORE_DESCENDANTS,
            a.beforeDescendants.getDescendantFocusability());
        assertEquals(ViewGroup.FOCUS_AFTER_DESCENDANTS,
            a.afterDescendants.getDescendantFocusability());
        assertEquals(ViewGroup.FOCUS_BLOCK_DESCENDANTS,
            a.blocksDescendants.getDescendantFocusability());
        assertTrue(a.beforeDescendantsChild.isFocusable());
        assertTrue(a.afterDescendantsChild.isFocusable());
        assertTrue(a.blocksDescendantsChild.isFocusable());
    }
    @UiThreadTest
    @MediumTest
    public void testBeforeDescendants() {
        a.beforeDescendants.setFocusable(true);
        assertTrue(a.beforeDescendants.requestFocus());
        assertTrue(a.beforeDescendants.isFocused());
        a.beforeDescendants.setFocusable(false);
        a.beforeDescendants.requestFocus();
        assertTrue(a.beforeDescendantsChild.isFocused());
    }
    @UiThreadTest
    @MediumTest
    public void testAfterDescendants() {
        a.afterDescendants.setFocusable(true);
        assertTrue(a.afterDescendants.requestFocus());
        assertTrue(a.afterDescendantsChild.isFocused());
        a.afterDescendants.setFocusable(false);
        assertTrue(a.afterDescendants.requestFocus());
        assertTrue(a.afterDescendantsChild.isFocused());
    }
    @UiThreadTest
    @MediumTest
    public void testBlocksDescendants() {
        a.blocksDescendants.setFocusable(true);
        assertTrue(a.blocksDescendants.requestFocus());
        assertTrue(a.blocksDescendants.isFocused());
        assertFalse(a.blocksDescendantsChild.isFocused());
        a.blocksDescendants.setFocusable(false);
        assertFalse(a.blocksDescendants.requestFocus());
        assertFalse(a.blocksDescendants.isFocused());
        assertFalse(a.blocksDescendantsChild.isFocused());
    }
    @UiThreadTest
    @MediumTest
    public void testChildOfDescendantBlockerRequestFocusFails() {
        assertFalse(a.blocksDescendantsChild.requestFocus());
    }
    @LargeTest
    public void testBeforeDescendantsEnterTouchMode() {
        a.runOnUiThread(new Runnable() {
            public void run() {
                a.beforeDescendants.setFocusableInTouchMode(true);
                a.beforeDescendantsChild.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(a.beforeDescendantsChild.isFocused());
        assertFalse(a.beforeDescendantsChild.isInTouchMode());
        TouchUtils.clickView(this, a.beforeDescendantsChild);
        assertTrue(a.beforeDescendantsChild.isInTouchMode());
        assertFalse(a.beforeDescendants.isFocused());        
    }
    @LargeTest
    public void testAfterDescendantsEnterTouchMode() {
        a.runOnUiThread(new Runnable() {
            public void run() {
                a.afterDescendants.setFocusableInTouchMode(true);
                a.afterDescendantsChild.requestFocus();
            }
        });
        getInstrumentation().waitForIdleSync();
        assertTrue(a.afterDescendantsChild.isFocused());
        assertFalse(a.afterDescendantsChild.isInTouchMode());
        TouchUtils.clickView(this, a.afterDescendantsChild);
        assertTrue(a.afterDescendantsChild.isInTouchMode());
        assertTrue(a.afterDescendants.isFocused());
    }
}
