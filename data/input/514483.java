public class ManyEditTextActivityScrollResizeTests extends ManyEditTextActivityBaseTestCase<ManyEditTextActivityScrollResize> {
    public final String TAG = "ManyEditTextActivityScrollResizeTests";
    public ManyEditTextActivityScrollResizeTests() {
        super(ManyEditTextActivityScrollResize.class);
    }
    @LargeTest
    public void testAllEditTextsAdjust() {
        verifyAllEditTextAdjustment(mTargetActivity.NUM_EDIT_TEXTS, 
                mTargetActivity.getRootView().getMeasuredHeight());
    }
}
