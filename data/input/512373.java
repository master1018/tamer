public class AWTEventListenerProxy extends EventListenerProxy implements AWTEventListener {
    private AWTEventListener listener;
    private long eventMask;
    public AWTEventListenerProxy(long eventMask, AWTEventListener listener) {
        super(listener);
        assert listener != null : Messages.getString("awt.193"); 
        this.listener = listener;
        this.eventMask = eventMask;
    }
    public void eventDispatched(AWTEvent evt) {
        listener.eventDispatched(evt);
    }
    public long getEventMask() {
        return eventMask;
    }
}
