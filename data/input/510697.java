public class CandidateViewButton extends Button {
    private int[] mUpState;
    public CandidateViewButton(Context context) {
        super(context);
    }
    public CandidateViewButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public boolean onTouchEvent(MotionEvent me) {
        boolean ret = super.onTouchEvent(me);
        Drawable d = getBackground();
        switch (me.getAction()) {
        case MotionEvent.ACTION_DOWN:
            mUpState = d.getState();
            d.setState(View.PRESSED_ENABLED_SELECTED_WINDOW_FOCUSED_STATE_SET);
            break;
        case MotionEvent.ACTION_UP:
        default:
            d.setState(mUpState);
            break;
        }
        return ret;
    }
}
