public class EventImpl implements Event {
    private String mEventType;
    private boolean mCanBubble;
    private boolean mCancelable;
    private boolean mInitialized;
    private EventTarget mTarget;
    private short mEventPhase;
    private boolean mStopPropagation;
    private boolean mPreventDefault;
    private EventTarget mCurrentTarget;
    private int mSeekTo;
    private final long mTimeStamp = System.currentTimeMillis();
    public boolean getBubbles() {
        return mCanBubble;
    }
    public boolean getCancelable() {
        return mCancelable;
    }
    public EventTarget getCurrentTarget() {
        return mCurrentTarget;
    }
    public short getEventPhase() {
        return mEventPhase;
    }
    public EventTarget getTarget() {
        return mTarget;
    }
    public long getTimeStamp() {
        return mTimeStamp;
    }
    public String getType() {
        return mEventType;
    }
    public void initEvent(String eventTypeArg, boolean canBubbleArg,
            boolean cancelableArg) {
        mEventType = eventTypeArg;
        mCanBubble = canBubbleArg;
        mCancelable = cancelableArg;
        mInitialized = true;
    }
    public void initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg,
            int seekTo) {
        mSeekTo = seekTo;
        initEvent(eventTypeArg, canBubbleArg, cancelableArg);
    }
    public void preventDefault() {
        mPreventDefault = true;
    }
    public void stopPropagation() {
        mStopPropagation = true;
    }
    boolean isInitialized() {
        return mInitialized;
    }
    boolean isPreventDefault() {
        return mPreventDefault;
    }
    boolean isPropogationStopped() {
        return mStopPropagation;
    }
    void setTarget(EventTarget target) {
        mTarget = target;
    }
    void setEventPhase(short eventPhase) {
        mEventPhase = eventPhase;
    }
    void setCurrentTarget(EventTarget currentTarget) {
        mCurrentTarget = currentTarget;
    }
    public int getSeekTo() {
        return mSeekTo;
    }
}
