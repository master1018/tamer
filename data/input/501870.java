public class ManyEditTextActivityScrollPanScanTests extends ManyEditTextActivityBaseTestCase<ManyEditTextActivityScrollPanScan> {
    public final String TAG = "ManyEditTextActivityScrollPanScanTests";
    public ManyEditTextActivityScrollPanScanTests() {
        super(ManyEditTextActivityScrollPanScan.class);
    }
    @LargeTest
    public void testAllEditTextsAdjust() {
        verifyAllEditTextAdjustment(mTargetActivity.NUM_EDIT_TEXTS, 
                mTargetActivity.getRootView().getMeasuredHeight());
    }
}
