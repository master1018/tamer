public class OneEditTextActivitySelectedTests extends ImfBaseTestCase<OneEditTextActivitySelected> {
    public final String TAG = "OneEditTextActivitySelectedTests";
    public OneEditTextActivitySelectedTests() {
        super(OneEditTextActivitySelected.class);
    }
    @LargeTest
    public void testSoftKeyboardAutoPop() {
        pause(2000);
        assertTrue(mImm.isAcceptingText());
        View rootView = ((OneEditTextActivitySelected) mTargetActivity).getRootView();
        View servedView = ((OneEditTextActivitySelected) mTargetActivity).getDefaultFocusedView();
        assertNotNull(rootView);
        assertNotNull(servedView);
        destructiveCheckImeInitialState(rootView, servedView);
        verifyEditTextAdjustment(servedView, rootView.getMeasuredHeight());
    }
}
