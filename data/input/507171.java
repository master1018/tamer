public class LatestItemView extends FrameLayout {
    public LatestItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return onTouchEvent(ev);
    }
}
