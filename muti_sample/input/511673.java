public class MonkeyMotionEvent extends MonkeyEvent {
    private long mDownTime = -1;
    private long mEventTime = -1;    
    private int mAction = -1;
    private float mX = -1;
    private float mY = -1;
    private float mPressure = -1;
    private float mSize = -1;
    private int mMetaState = -1;
    private float mXPrecision = -1;
    private float mYPrecision = -1;
    private int mDeviceId = -1;
    private int mEdgeFlags = -1;
    private boolean mIntermediateNote;  
    public MonkeyMotionEvent(int type, long downAt, int action, 
            float x, float y, int metaState) {
        super(type);
        mDownTime = downAt;
        mAction = action;
        mX = x;
        mY = y;
        mMetaState = metaState;
    }
    public MonkeyMotionEvent(int type, long downTime, long eventTime, int action,
            float x, float y, float pressure, float size, int metaState,
            float xPrecision, float yPrecision, int deviceId, int edgeFlags) {
        super(type);
        mDownTime = downTime;
        mEventTime = eventTime;
        mAction = action;
        mX = x;
        mY = y;
        mPressure = pressure;
        mSize = size;
        mMetaState = metaState;
        mXPrecision = xPrecision;
        mYPrecision = yPrecision;
        mDeviceId = deviceId;
        mEdgeFlags = edgeFlags;
    }    
    public void setIntermediateNote(boolean b) {
        mIntermediateNote = b;
    }
    public boolean getIntermediateNote() {
        return mIntermediateNote;
    }
    public float getX() {
        return mX;
    }
    public float getY() {
        return mY;
    }
    public int getAction() {
        return mAction;
    }
    public long getDownTime() {
        return mDownTime;
    }
    public long getEventTime() {
        return mEventTime;
    }
    public void setDownTime(long downTime) {
        mDownTime = downTime;
    }
    public void setEventTime(long eventTime) {
        mEventTime = eventTime;
    }
    private MotionEvent getEvent() {
        if (mDeviceId < 0) {
            return MotionEvent.obtain(mDownTime, SystemClock.uptimeMillis(), 
                mAction, mX, mY, mMetaState);
        }
        return MotionEvent.obtain(mDownTime, mEventTime, 
                mAction, mX, mY, mPressure, mSize, mMetaState,
                mXPrecision, mYPrecision, mDeviceId, mEdgeFlags);
    }
    @Override
    public boolean isThrottlable() {
        return (getAction() == KeyEvent.ACTION_UP);
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        String note;
        if ((verbose > 0 && !mIntermediateNote) || verbose > 1) {
            if (mAction == MotionEvent.ACTION_DOWN) {
                note = "DOWN";
            } else if (mAction == MotionEvent.ACTION_UP) {
                note = "UP";
            } else {
                note = "MOVE";
            }
            System.out.println(":Sending Pointer ACTION_" + note + 
                    " x=" + mX + " y=" + mY);
        }
        try {
            int type = this.getEventType();
            MotionEvent me = getEvent();
            if ((type == MonkeyEvent.EVENT_TYPE_POINTER && 
                    !iwm.injectPointerEvent(me, false))
                    || (type == MonkeyEvent.EVENT_TYPE_TRACKBALL && 
                            !iwm.injectTrackballEvent(me, false))) {
                return MonkeyEvent.INJECT_FAIL;
            }
        } catch (RemoteException ex) {
            return MonkeyEvent.INJECT_ERROR_REMOTE_EXCEPTION;
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
