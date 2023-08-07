public abstract class AbstractEvent implements Event {
    protected String type;
    protected boolean isBubbling;
    protected boolean cancelable;
    protected EventTarget currentTarget;
    protected EventTarget target;
    protected short eventPhase;
    protected long timeStamp = System.currentTimeMillis();
    protected boolean stopPropagation = false;
    protected boolean preventDefault = false;
    public String getType() {
        return type;
    }
    public EventTarget getCurrentTarget() {
        return currentTarget;
    }
    public EventTarget getTarget() {
        return target;
    }
    public short getEventPhase() {
        return eventPhase;
    }
    public boolean getBubbles() {
        return isBubbling;
    }
    public boolean getCancelable() {
        return cancelable;
    }
    public long getTimeStamp() {
        return timeStamp;
    }
    public void stopPropagation() {
        this.stopPropagation = true;
    }
    public void preventDefault() {
        this.preventDefault = true;
    }
    public void initEvent(String eventTypeArg, boolean canBubbleArg, boolean cancelableArg) {
        this.type = eventTypeArg;
        this.isBubbling = canBubbleArg;
        this.cancelable = cancelableArg;
    }
    boolean getPreventDefault() {
        return preventDefault;
    }
    boolean getStopPropagation() {
        return stopPropagation;
    }
    void setEventPhase(short eventPhase) {
        this.eventPhase = eventPhase;
    }
    void stopPropagation(boolean state) {
        this.stopPropagation = state;
    }
    void preventDefault(boolean state) {
        this.preventDefault = state;
    }
    void setCurrentTarget(EventTarget currentTarget) {
        this.currentTarget = currentTarget;
    }
    void setTarget(EventTarget target) {
        this.target = target;
    }
    public static boolean getEventPreventDefault(Event evt) {
        AbstractEvent ae = (AbstractEvent) evt;
        return ae.getPreventDefault();
    }
}
