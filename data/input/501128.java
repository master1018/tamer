public class TouchDelegate {
    private View mDelegateView;
    private Rect mBounds;
    private Rect mSlopBounds;
    private boolean mDelegateTargeted;
    public static final int ABOVE = 1;
    public static final int BELOW = 2;
    public static final int TO_LEFT = 4;
    public static final int TO_RIGHT = 8;
    private int mSlop;
    public TouchDelegate(Rect bounds, View delegateView) {
        mBounds = bounds;
        mSlop = ViewConfiguration.get(delegateView.getContext()).getScaledTouchSlop();
        mSlopBounds = new Rect(bounds);
        mSlopBounds.inset(-mSlop, -mSlop);
        mDelegateView = delegateView;
    }
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int)event.getX();
        int y = (int)event.getY();
        boolean sendToDelegate = false;
        boolean hit = true;
        boolean handled = false;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            Rect bounds = mBounds;
            if (bounds.contains(x, y)) {
                mDelegateTargeted = true;
                sendToDelegate = true;
            }
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_MOVE:
            sendToDelegate = mDelegateTargeted;
            if (sendToDelegate) {
                Rect slopBounds = mSlopBounds;
                if (!slopBounds.contains(x, y)) {
                    hit = false;
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
            sendToDelegate = mDelegateTargeted;
            mDelegateTargeted = false;
            break;
        }
        if (sendToDelegate) {
            final View delegateView = mDelegateView;
            if (hit) {
                event.setLocation(delegateView.getWidth() / 2, delegateView.getHeight() / 2);
            } else {
                int slop = mSlop;
                event.setLocation(-(slop * 2), -(slop * 2));
            }
            handled = delegateView.dispatchTouchEvent(event);
        }
        return handled;
    }
}
