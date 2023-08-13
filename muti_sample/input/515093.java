public class MenuWith1ItemTest extends ActivityInstrumentationTestCase<MenuWith1Item> {
    private MenuWith1Item mActivity;
    public MenuWith1ItemTest() {
        super("com.android.frameworks.coretests", MenuWith1Item.class);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mActivity = getActivity();
    }
    @MediumTest
    public void testPreconditions() {
        assertNotNull(mActivity);
        assertFalse(mActivity.getButton().isInTouchMode());
    }
    @MediumTest
    public void testItemClick() {
        KeyUtils.tapMenuKey(this);
        getInstrumentation().waitForIdleSync();
        assertFalse("Item seems to have been clicked before we clicked on it", mActivity
                .wasItemClicked(0));
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        assertTrue("Item doesn't seem to have registered our click", mActivity.wasItemClicked(0));
    }
    @LargeTest
    public void testTouchModeTransfersRemovesFocus() throws Exception {
        sendKeys(KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_DPAD_LEFT);
        final View menuItem = mActivity.getItemView(MenuBuilder.TYPE_ICON, 0);
        assertTrue("menuItem.isFocused()", menuItem.isFocused());
        sendKeys(KeyEvent.KEYCODE_MENU);
        Thread.sleep(500);
        TouchUtils.clickView(this, mActivity.getButton());
        assertTrue("should be in touch mode after touching button",
                mActivity.getButton().isInTouchMode());
        sendKeys(KeyEvent.KEYCODE_MENU);
        assertTrue("menuItem.isInTouchMode()", menuItem.isInTouchMode());
        assertFalse("menuItem.isFocused()", menuItem.isFocused());
    }
}
