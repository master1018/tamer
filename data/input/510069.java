public class MonkeyKeyEvent extends MonkeyEvent {
    private long mDownTime = -1;
    private int mMetaState = -1;
    private int mAction = -1;
    private int mKeyCode = -1;
    private int mScancode = -1;
    private int mRepeatCount = -1;
    private int mDeviceId = -1;
    private long mEventTime = -1;
    private KeyEvent keyEvent = null;
    public MonkeyKeyEvent(int action, int keycode) {
        super(EVENT_TYPE_KEY);
        mAction = action;
        mKeyCode = keycode;
    }
    public MonkeyKeyEvent(KeyEvent e) {
        super(EVENT_TYPE_KEY);
        keyEvent = e;
    }
    public MonkeyKeyEvent(long downTime, long eventTime, int action,
            int code, int repeat, int metaState,
            int device, int scancode) {
        super(EVENT_TYPE_KEY);
        mAction = action;
        mKeyCode = code;
        mMetaState = metaState;
        mScancode = scancode;
        mRepeatCount = repeat;
        mDeviceId = device;
        mDownTime = downTime;
        mEventTime = eventTime;
    }
    public int getKeyCode() {
        return mKeyCode;
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
    private KeyEvent getEvent() {
        if (keyEvent == null) {
            if (mDeviceId < 0) {
                keyEvent = new KeyEvent(mAction, mKeyCode);
            } else {
                keyEvent = new KeyEvent(mDownTime, mEventTime, mAction,
                                        mKeyCode, mRepeatCount, mMetaState, mDeviceId, mScancode);
            }
        }
        return keyEvent;
    }
    @Override
    public boolean isThrottlable() {
        return (getAction() == KeyEvent.ACTION_UP);
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 1) {
            String note;
            if (mAction == KeyEvent.ACTION_UP) {
                note = "ACTION_UP";
            } else {
                note = "ACTION_DOWN";
            }
            try {
                System.out.println(":SendKey (" + note + "): "
                        + mKeyCode + "    
                        + MonkeySourceRandom.getKeyName(mKeyCode));
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(":SendKey (ACTION_UP): "
                        + mKeyCode + "    
            }
        }
        try {
            if (!iwm.injectKeyEvent(getEvent(), false)) {
                return MonkeyEvent.INJECT_FAIL;
            }
        } catch (RemoteException ex) {
            return MonkeyEvent.INJECT_ERROR_REMOTE_EXCEPTION;
        }
        return MonkeyEvent.INJECT_SUCCESS;
    }
}
