public class ZoomButton extends ImageButton implements OnLongClickListener {
    private final Handler mHandler;
    private final Runnable mRunnable = new Runnable() {
        public void run() {
            if ((mOnClickListener != null) && mIsInLongpress && isEnabled()) {
                mOnClickListener.onClick(ZoomButton.this);
                mHandler.postDelayed(this, mZoomSpeed);
            }
        }
    };
    private long mZoomSpeed = 1000;
    private boolean mIsInLongpress;
    public ZoomButton(Context context) {
        this(context, null);
    }
    public ZoomButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public ZoomButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mHandler = new Handler();
        setOnLongClickListener(this);
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getAction() == MotionEvent.ACTION_CANCEL)
                || (event.getAction() == MotionEvent.ACTION_UP)) {
            mIsInLongpress = false;
        }
        return super.onTouchEvent(event);
    }
    public void setZoomSpeed(long speed) {
        mZoomSpeed = speed;
    }
    public boolean onLongClick(View v) {
        mIsInLongpress = true;
        mHandler.post(mRunnable);
        return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mIsInLongpress = false;
        return super.onKeyUp(keyCode, event);
    }
    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            setPressed(false);
        }
        super.setEnabled(enabled);
    }
    @Override
    public boolean dispatchUnhandledMove(View focused, int direction) {
        clearFocus();
        return super.dispatchUnhandledMove(focused, direction);
    }
}
