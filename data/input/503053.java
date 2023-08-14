class SymmetricalLinearTween {
    private static final int FPS = 30;
    private static final int FRAME_TIME = 1000 / FPS;
    Handler mHandler;
    int mDuration;
    TweenCallback mCallback;
    boolean mRunning;
    long mBase;
    boolean mDirection;
    float mValue;
    public SymmetricalLinearTween(boolean initial, int duration, TweenCallback callback) {
        mValue = initial ? 1.0f : 0.0f;
        mDirection = initial;
        mDuration = duration;
        mCallback = callback;
        mHandler = new Handler();
    }
    public void start(boolean direction) {
        start(direction, SystemClock.uptimeMillis());
    }
    public void start(boolean direction, long baseTime) {
        if (direction != mDirection) {
            if (!mRunning) {
                mBase = baseTime;
                mRunning = true;
                mCallback.onTweenStarted();
                long next = SystemClock.uptimeMillis() + FRAME_TIME;
                mHandler.postAtTime(mTick, next);
            } else {
                long now = SystemClock.uptimeMillis();
                long diff = now - mBase;
                mBase = now + diff - mDuration;
            }
            mDirection = direction;
        }
    }
    Runnable mTick = new Runnable() {
        public void run() {
            long base = mBase;
            long now = SystemClock.uptimeMillis();
            long diff = now-base;
            int duration = mDuration;
            float val = diff/(float)duration;
            if (!mDirection) {
                val = 1.0f - val;
            }
            if (val > 1.0f) {
                val = 1.0f;
            } else if (val < 0.0f) {
                val = 0.0f;
            }
            float old = mValue;
            mValue = val;
            mCallback.onTweenValueChanged(val, old);
            int frame = (int)(diff / FRAME_TIME);
            long next = base + ((frame+1)*FRAME_TIME);
            if (diff < duration) {
                mHandler.postAtTime(this, next);
            }
            if (diff >= duration) {
                mCallback.onTweenFinished();
                mRunning = false;
            }
        }
    };
}
