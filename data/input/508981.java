public class BigEditTextActivityNonScrollableResizeTests extends ImfBaseTestCase<BigEditTextActivityNonScrollableResize> {
	public final String TAG = "BigEditTextActivityNonScrollableResizeTests";
    public BigEditTextActivityNonScrollableResizeTests() {
        super(BigEditTextActivityNonScrollableResize.class);
    }
	@LargeTest
	public void testAppAdjustmentPanScan() {       
        pause(2000);
        View rootView = ((BigEditTextActivityNonScrollableResize) mTargetActivity).getRootView();
        View servedView = ((BigEditTextActivityNonScrollableResize) mTargetActivity).getDefaultFocusedView();
        assertNotNull(rootView);
        assertNotNull(servedView);
        destructiveCheckImeInitialState(rootView, servedView);
        verifyEditTextAdjustment(servedView, rootView.getMeasuredHeight());
	}
}
