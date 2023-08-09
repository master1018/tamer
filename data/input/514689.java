public abstract class MonkeyEvent {
    protected int eventType;
    public static final int EVENT_TYPE_KEY = 0;
    public static final int EVENT_TYPE_POINTER = 1;
    public static final int EVENT_TYPE_TRACKBALL = 2;
    public static final int EVENT_TYPE_ACTIVITY = 3;
    public static final int EVENT_TYPE_FLIP = 4; 
    public static final int EVENT_TYPE_THROTTLE = 5;
    public static final int EVENT_TYPE_NOOP = 6;
    public static final int INJECT_SUCCESS = 1;
    public static final int INJECT_FAIL = 0;
    public static final int INJECT_ERROR_REMOTE_EXCEPTION = -1;
    public static final int INJECT_ERROR_SECURITY_EXCEPTION = -2;
    public MonkeyEvent(int type) {
        eventType = type;
    }
    public int getEventType() {
        return eventType;
    }
    public boolean isThrottlable() {
        return true;
    }
    public abstract int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose);
}
