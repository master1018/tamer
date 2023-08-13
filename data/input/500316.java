@TestTargetClass(ClipboardManager.class)
public class ClipboardManagerTest extends AndroidTestCase {
    private ClipboardManager mClipboardManager;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mClipboardManager = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "setText",
            args = {CharSequence.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            method = "getText",
            args = {}
        )
    })
    public void testAccessText() {
        CharSequence expected = "test";
        mClipboardManager.setText(expected);
        assertEquals(expected, mClipboardManager.getText());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        method = "hasText",
        args = {}
    )
    public void testHasText() {
        mClipboardManager.setText("");
        assertFalse(mClipboardManager.hasText());
        mClipboardManager.setText("test");
        assertTrue(mClipboardManager.hasText());
        mClipboardManager.setText(null);
        assertFalse(mClipboardManager.hasText());
    }
}
