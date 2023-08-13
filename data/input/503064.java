public class ButtonActivityTest extends ImfBaseTestCase<ButtonActivity> {
	final public String TAG = "ButtonActivityTest";
    public ButtonActivityTest() {
        super(ButtonActivity.class);
    }
    @LargeTest
    public void testButtonActivatesIme() {
        final Button button = (Button) mTargetActivity.findViewById(ButtonActivity.BUTTON_ID);
        mTargetActivity.runOnUiThread(new Runnable() {
            public void run() {
                button.requestFocus();
            }
        });
        sendKeys(KeyEvent.KEYCODE_DPAD_CENTER);
        pause(2000);
        assertTrue(mImm.isActive());
        assertFalse(mImm.isAcceptingText());
        destructiveCheckImeInitialState(mTargetActivity.getRootView(), button);
    }
}
