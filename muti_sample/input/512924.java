public class BigEditTextActivityNonScrollablePanScanTests extends ImfBaseTestCase<BigEditTextActivityNonScrollablePanScan> {
	public final String TAG = "BigEditTextActivityNonScrollablePanScanTests";
    public BigEditTextActivityNonScrollablePanScanTests() {
        super(BigEditTextActivityNonScrollablePanScan.class);
    }
	@LargeTest
	public void testAppAdjustmentPanScan() {
	    pause(2000);
	    View rootView = ((BigEditTextActivityNonScrollablePanScan) mTargetActivity).getRootView();
	    View servedView = ((BigEditTextActivityNonScrollablePanScan) mTargetActivity).getDefaultFocusedView();
	    assertNotNull(rootView);
	    assertNotNull(servedView);
	    destructiveCheckImeInitialState(rootView, servedView);
        verifyEditTextAdjustment(servedView, rootView.getMeasuredHeight());
	}
}
