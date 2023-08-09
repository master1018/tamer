public class LinearLayoutWithDefaultTouchRecepient extends LinearLayout {
    private final Rect mTempRect = new Rect();
    private View mDefaultTouchRecepient;
    public LinearLayoutWithDefaultTouchRecepient(Context context) {
        super(context);
    }
    public LinearLayoutWithDefaultTouchRecepient(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setDefaultTouchRecepient(View defaultTouchRecepient) {
        mDefaultTouchRecepient = defaultTouchRecepient;
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mDefaultTouchRecepient == null) {
            return super.dispatchTouchEvent(ev);
        }
        if (super.dispatchTouchEvent(ev)) {
            return true;
        }
        mTempRect.set(0, 0, 0, 0);
        offsetRectIntoDescendantCoords(mDefaultTouchRecepient, mTempRect);
        ev.setLocation(ev.getX() + mTempRect.left, ev.getY() + mTempRect.top);
        return mDefaultTouchRecepient.dispatchTouchEvent(ev);
    }
}
