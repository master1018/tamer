public class BigEditTextActivityScrollableResizeTests extends ImfBaseTestCase<BigEditTextActivityScrollableResize> {
	public final String TAG = "BigEditTextActivityScrollableResizeTests";
    public BigEditTextActivityScrollableResizeTests() {
        super(BigEditTextActivityScrollableResize.class);
    }
	@LargeTest
	public void testAppAdjustmentPanScan() {       
        pause(2000);
        View rootView = ((BigEditTextActivityScrollableResize) mTargetActivity).getRootView();
        View servedView = ((BigEditTextActivityScrollableResize) mTargetActivity).getDefaultFocusedView();
        assertNotNull(rootView);
        assertNotNull(servedView);
        destructiveCheckImeInitialState(rootView, servedView);
        verifyEditTextAdjustment(servedView, rootView.getMeasuredHeight());
	}
}
