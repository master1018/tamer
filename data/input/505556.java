public class BottomEditTextActivityResizeTests extends ImfBaseTestCase<BottomEditTextActivityResize> {
    public final String TAG = "BottomEditTextActivityResizeTests";
    public BottomEditTextActivityResizeTests() {
        super(BottomEditTextActivityResize.class);
    }
    @LargeTest
    public void testAppAdjustmentResize() {
        pause(2000);
        View rootView = ((BottomEditTextActivityResize) mTargetActivity).getRootView();
        View servedView = ((BottomEditTextActivityResize) mTargetActivity).getDefaultFocusedView();
        assertNotNull(rootView);
        assertNotNull(servedView);
        destructiveCheckImeInitialState(rootView, servedView);
        verifyEditTextAdjustment(servedView, rootView.getMeasuredHeight());
    }
}
