public class BottomEditTextActivityPanScanTests extends ImfBaseTestCase<BottomEditTextActivityPanScan> {
	public final String TAG = "BottomEditTextActivityPanScanTests";
    public BottomEditTextActivityPanScanTests() {
        super(BottomEditTextActivityPanScan.class);
    }
	@LargeTest
	public void testAppAdjustmentPanScan() {
        pause(2000);
        View rootView = ((BottomEditTextActivityPanScan) mTargetActivity).getRootView();
        View servedView = ((BottomEditTextActivityPanScan) mTargetActivity).getDefaultFocusedView();
        assertNotNull(rootView);
        assertNotNull(servedView);
        destructiveCheckImeInitialState(rootView, servedView);
        verifyEditTextAdjustment(servedView, rootView.getMeasuredHeight());
	}
}
