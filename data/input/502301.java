public class OneEditTextActivityNotSelectedTests extends ImfBaseTestCase<OneEditTextActivityNotSelected> {
	public final String TAG = "OneEditTextActivityNotSelectedTests";
    public OneEditTextActivityNotSelectedTests() {
        super(OneEditTextActivityNotSelected.class);
    }
	@LargeTest
	public void testSoftKeyboardNoAutoPop() {
	    pause(2000);
	    assertFalse(mImm.isAcceptingText());
	    View rootView = ((OneEditTextActivityNotSelected) mTargetActivity).getRootView();
        View servedView = ((OneEditTextActivityNotSelected) mTargetActivity).getDefaultFocusedView();
        assertNotNull(rootView);
        assertNotNull(servedView);
        destructiveCheckImeInitialState(rootView, servedView);
        verifyEditTextAdjustment(servedView, rootView.getMeasuredHeight());
	}
}
