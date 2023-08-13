public class MockLinearLayout extends LinearLayout {
    public boolean mIsInvalidateChildInParentCalled;
    public MockLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MockLinearLayout(Context context) {
        super(context);
    }
    @Override
    public ViewParent invalidateChildInParent(int[] location, Rect dirty) {
        mIsInvalidateChildInParentCalled = true;
        return super.invalidateChildInParent(location, dirty);
    }
}
