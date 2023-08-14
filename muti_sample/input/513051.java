public class ExpandedView extends LinearLayout {
    StatusBarService mService;
    int mPrevHeight = -1;
    public ExpandedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
    @Override
    public int getSuggestedMinimumHeight() {
        return 0;
    }
    @Override
     protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
         super.onLayout(changed, left, top, right, bottom);
         int height = bottom - top;
         if (height != mPrevHeight) {
             mPrevHeight = height;
             mService.updateExpandedViewPos(StatusBarService.EXPANDED_LEAVE_ALONE);
         }
     }
}
