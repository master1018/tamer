public class ManyEditTextActivityNoScrollPanScanTests extends ManyEditTextActivityBaseTestCase<ManyEditTextActivityNoScrollPanScan> {
    public final String TAG = "ManyEditTextActivityNoScrollPanScanTests";
    public ManyEditTextActivityNoScrollPanScanTests() {
        super(ManyEditTextActivityNoScrollPanScan.class);
    }
    @LargeTest
    public void testAllEditTextsAdjust() {
        verifyAllEditTextAdjustment(mTargetActivity.NUM_EDIT_TEXTS, 
                mTargetActivity.getRootView().getMeasuredHeight());
    }
}
